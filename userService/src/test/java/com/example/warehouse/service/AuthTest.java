package com.example.warehouse.service;

import com.example.warehouse.auth.AuthController;
import com.example.warehouse.auth.jwt.JWTUtils;
import com.example.warehouse.dto.UserDTO.AuthRequestDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTest {

    @Autowired
    private WebTestClient webTestClient;

    @Mock
    private UserService userService;

    @Mock
    private JWTUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(RoleType.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        invalidToken = "invalid.token.string";
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("john.doe@example.com", "correctPassword");

        when(userService.loginUser(request.email(), request.password()))
                .thenReturn(Mono.just(testUser));
        when(jwtUtils.generateTokenFromUsername("1"))
                .thenReturn(validToken);

        // Act & Assert
        StepVerifier.create(authController.login(request))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    assertTrue(response.getBody().contains("\"token\":\"" + validToken + "\""));
                })
                .verifyComplete();

        verify(userService).loginUser(request.email(), request.password());
        verify(jwtUtils).generateTokenFromUsername("1");
    }

    @Test
    void login_ShouldReturn500_WhenTokenGenerationFails() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("john.doe@example.com", "correctPassword");

        when(userService.loginUser(request.email(), request.password()))
                .thenReturn(Mono.just(testUser));
        when(jwtUtils.generateTokenFromUsername("1"))
                .thenThrow(new RuntimeException("JWT generation error"));

        // Act & Assert
        StepVerifier.create(authController.login(request))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    assertTrue(response.getBody().contains("Token generation failed"));
                })
                .verifyComplete();

        verify(userService).loginUser(request.email(), request.password());
        verify(jwtUtils).generateTokenFromUsername("1");
    }

    @Test
    void login_ShouldReturn500_WhenGenericExceptionOccurs() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("john.doe@example.com", "password");

        when(userService.loginUser(request.email(), request.password()))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        // Act & Assert
        StepVerifier.create(authController.login(request))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertTrue(response.getBody().contains("Authentication service error"));
                    assertTrue(response.getBody().contains("Database connection failed"));
                })
                .verifyComplete();

        verify(userService).loginUser(request.email(), request.password());
    }

    @Test
    void validate_ShouldReturnUserDetails_WhenTokenIsValidAndUserExists() {
        // Arrange
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn("1");
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(authController.validate(validToken))
                .assertNext(userDetails -> {
                    assertNotNull(userDetails);
                    assertEquals("John", userDetails.getUsername());
                    assertArrayEquals(new String[]{"ROLE_ADMIN"}, userDetails.getAuthorities());
                    assertTrue(userDetails.getAccountNonExpired());
                    assertTrue(userDetails.getAccountNonLocked());
                    assertTrue(userDetails.getCredentialsNonExpired());
                    assertTrue(userDetails.getEnabled());
                })
                .verifyComplete();

        verify(jwtUtils).validateJwtToken(validToken);
        verify(jwtUtils).getUserNameFromJwtToken(validToken);
        verify(userService).getUserById(1L);
    }

    @Test
    void validate_ShouldThrowAccessDeniedException_WhenTokenIsInvalid() {
        // Arrange
        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

        // Act & Assert
        StepVerifier.create(authController.validate(invalidToken))
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("Not valid")
                )
                .verify();

        verify(jwtUtils).validateJwtToken(invalidToken);
        verifyNoMoreInteractions(jwtUtils);
        verifyNoInteractions(userService);
    }

    @Test
    void validate_ShouldHandleEmptyToken() {
        // Arrange
        String emptyToken = "";
        when(jwtUtils.validateJwtToken(emptyToken)).thenReturn(false);

        // Act & Assert
        StepVerifier.create(authController.validate(emptyToken))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(jwtUtils).validateJwtToken(emptyToken);
    }

    @Test
    void validate_ShouldHandleDifferentUserRoles() {
        // Arrange
        User studentUser = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .role(RoleType.STUDENT)
                .build();

        String studentToken = "student.token.here";

        when(jwtUtils.validateJwtToken(studentToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(studentToken)).thenReturn("2");
        when(userService.getUserById(2L)).thenReturn(Mono.just(studentUser));

        // Act & Assert
        StepVerifier.create(authController.validate(studentToken))
                .assertNext(userDetails -> {
                    assertEquals("Jane", userDetails.getUsername());
                    assertArrayEquals(new String[]{"ROLE_STUDENT"}, userDetails.getAuthorities());
                })
                .verifyComplete();

        verify(jwtUtils).validateJwtToken(studentToken);
        verify(jwtUtils).getUserNameFromJwtToken(studentToken);
        verify(userService).getUserById(2L);
    }

    @Test
    void validate_ShouldSetAllBooleanFlagsToTrue() {
        // Arrange
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn("1");
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(authController.validate(validToken))
                .assertNext(userDetails -> {
                    assertTrue(userDetails.getAccountNonExpired());
                    assertTrue(userDetails.getAccountNonLocked());
                    assertTrue(userDetails.getCredentialsNonExpired());
                    assertTrue(userDetails.getEnabled());
                })
                .verifyComplete();
    }

    @Test
    void validate_ShouldHandleMalformedJWTToken() {
        // Arrange
        String malformedToken = "header.payload";
        when(jwtUtils.validateJwtToken(malformedToken)).thenReturn(false);

        // Act & Assert
        StepVerifier.create(authController.validate(malformedToken))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(jwtUtils).validateJwtToken(malformedToken);
    }

    @Test
    void validate_ShouldHandleExpiredJWTToken() {
        // Arrange
        String expiredToken = "expired.token.here";
        when(jwtUtils.validateJwtToken(expiredToken)).thenReturn(false);

        // Act & Assert
        StepVerifier.create(authController.validate(expiredToken))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(jwtUtils).validateJwtToken(expiredToken);
    }

    @Test
    void validate_ShouldHandleServiceUnavailable() {
        // Arrange
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn("1");
        when(userService.getUserById(1L))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Act & Assert
        StepVerifier.create(authController.validate(validToken))
                .expectError(RuntimeException.class)
                .verify();

        verify(jwtUtils).validateJwtToken(validToken);
        verify(jwtUtils).getUserNameFromJwtToken(validToken);
        verify(userService).getUserById(1L);
    }

    @Test
    void validate_ShouldHandleDatabaseException() {
        // Arrange
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn("1");
        when(userService.getUserById(1L))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        // Act & Assert
        StepVerifier.create(authController.validate(validToken))
                .expectError(RuntimeException.class)
                .verify();

        verify(jwtUtils).validateJwtToken(validToken);
        verify(jwtUtils).getUserNameFromJwtToken(validToken);
        verify(userService).getUserById(1L);
    }

    @Test
    void login_ShouldHandleDifferentMediaTypes() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("john.doe@example.com", "password");

        when(userService.loginUser(request.email(), request.password()))
                .thenReturn(Mono.just(testUser));
        when(jwtUtils.generateTokenFromUsername("1"))
                .thenReturn(validToken);

        // Act & Assert
        StepVerifier.create(authController.login(request))
                .assertNext(response -> {
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    assertNotNull(response.getHeaders().getContentType());
                })
                .verifyComplete();
    }

    @Test
    void login_ShouldReturnValidJsonStructure() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("test@example.com", "password");

        when(userService.loginUser(anyString(), anyString()))
                .thenReturn(Mono.just(testUser));
        when(jwtUtils.generateTokenFromUsername(anyString()))
                .thenReturn("mock-token");

        // Act & Assert
        StepVerifier.create(authController.login(request))
                .assertNext(response -> {
                    String body = response.getBody();
                    assertTrue(body.startsWith("{"));
                    assertTrue(body.endsWith("}"));
                    assertTrue(body.contains("\"token\""));
                    assertTrue(body.contains("mock-token"));
                    // Проверяем что нет лишних полей
                    assertFalse(body.contains("\"error\""));
                })
                .verifyComplete();
    }

    @Test
    void login_ShouldReturnErrorJsonStructure_WhenErrorOccurs() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("test@example.com", "password");

        when(userService.loginUser(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Test error")));

        // Act & Assert
        StepVerifier.create(authController.login(request))
                .assertNext(response -> {
                    String body = response.getBody();
                    assertTrue(body.startsWith("{"));
                    assertTrue(body.endsWith("}"));
                    assertTrue(body.contains("\"error\""));
                    assertTrue(body.contains("Test error"));
                })
                .verifyComplete();
    }
}
