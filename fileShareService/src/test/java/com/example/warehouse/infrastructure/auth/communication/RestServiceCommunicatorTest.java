package com.example.warehouse.infrastructure.auth.communication;

import com.example.warehouse.infrastructure.auth.AuthRequest;
import com.example.warehouse.infrastructure.auth.RequestType;
import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestServiceCommunicatorTest {

    @Mock
    private UserServiceClient userServiceClient;

    private RestServiceCommunicator communicator;

    @BeforeEach
    void setUp() {
        communicator = new RestServiceCommunicator(userServiceClient);
    }

    @Test
    void constructor_ShouldInitializeWithUserServiceClient() {
        RestServiceCommunicator comm = new RestServiceCommunicator(userServiceClient);
        assertNotNull(comm);
    }

    @Test
    void executeRequest_WhenGetTypeAndUserFound_ShouldReturnSuccessResult() {
        String username = "testuser";
        AuthRequest request = new AuthRequest("id", RequestType.GET_USER, username);
        UserDetailsEntity userDetailsEntity = new UserDetailsEntity();
        
        // Set up the entity with test data using reflection
        setUserDetailsEntityField(userDetailsEntity, "username", username);
        
        when(userServiceClient.checkUserAuth(username)).thenReturn(Mono.just(userDetailsEntity));

        Mono<AuthRequest> result = communicator.executeRequest(request);

        StepVerifier.create(result)
                .assertNext(authRequest -> {
                    assertEquals(AuthRequest.SUCCESS, authRequest.getStatus());
                    assertNotNull(authRequest.getResult());
                })
                .verifyComplete();
    }

    @Test
    void executeRequest_WhenGetTypeAndUserNotFound_ShouldReturnErrorResult() {
        String username = "nonexistentuser";
        AuthRequest request = new AuthRequest("id", RequestType.GET_USER, username);
        
        when(userServiceClient.checkUserAuth(username)).thenReturn(Mono.empty());

        Mono<AuthRequest> result = communicator.executeRequest(request);

        StepVerifier.create(result)
                .assertNext(authRequest -> {
                    assertEquals(AuthRequest.ERROR, authRequest.getStatus());
                    assertEquals("User not found", authRequest.getResult());
                })
                .verifyComplete();
    }

    @Test
    void executeRequest_WhenUnsupportedType_ShouldReturnErrorResult() {
        AuthRequest request = new AuthRequest("id", RequestType.REGISTER, "payload");

        Mono<AuthRequest> result = communicator.executeRequest(request);

        StepVerifier.create(result)
                .assertNext(authRequest -> {
                    assertEquals(AuthRequest.ERROR, authRequest.getStatus());
                    assertTrue(((String) authRequest.getResult()).contains("Unsupported request type"));
                })
                .verifyComplete();
    }

    @Test
    void executeRequest_WhenExceptionOccurs_ShouldReturnErrorResult() {
        String username = "testuser";
        AuthRequest request = new AuthRequest("id", RequestType.GET_USER, username);
        
        when(userServiceClient.checkUserAuth(username)).thenThrow(new RuntimeException("Service error"));

        Mono<AuthRequest> result = communicator.executeRequest(request);

        StepVerifier.create(result)
                .assertNext(authRequest -> {
                    assertEquals(AuthRequest.ERROR, authRequest.getStatus());
                    assertTrue(((String) authRequest.getResult()).contains("Service unavailable"));
                })
                .verifyComplete();
    }

    @Test
    void executeRequest_WhenUserDetailsConversionFails_ShouldReturnErrorResult() {
        String username = "testuser";
        AuthRequest request = new AuthRequest("id", RequestType.GET_USER, username);
        UserDetailsEntity userDetailsEntity = new UserDetailsEntity(); // Empty entity
        
        when(userServiceClient.checkUserAuth(username)).thenReturn(Mono.just(userDetailsEntity));

        Mono<AuthRequest> result = communicator.executeRequest(request);

        StepVerifier.create(result)
                .assertNext(authRequest -> {
                    assertEquals(AuthRequest.SUCCESS, authRequest.getStatus());
                    assertEquals("User conversion failed", authRequest.getResult());
                })
                .verifyComplete();
    }

    // Helper method to set private fields using reflection for testing
    private void setUserDetailsEntityField(UserDetailsEntity entity, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = UserDetailsEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            fail("Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }
}
