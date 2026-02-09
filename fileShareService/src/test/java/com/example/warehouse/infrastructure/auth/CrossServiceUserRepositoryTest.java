package com.example.warehouse.infrastructure.auth;

import com.example.warehouse.infrastructure.auth.communication.RestServiceCommunicator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrossServiceUserRepositoryTest {

    @Mock
    private RestServiceCommunicator communicator;

    private CrossServiceUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CrossServiceUserRepository(communicator);
    }

    @Test
    void constructor_ShouldInitializeWithCommunicator() {
        CrossServiceUserRepository repo = new CrossServiceUserRepository(communicator);
        assertNotNull(repo);
    }

    @Test
    void getUserDetailsService_ShouldReturnUserDetailsService() {
        UserDetailsService service = repository.getUserDetailsService();
        assertNotNull(service);
    }

    @Test
    void getUserDetailsPasswordService_ShouldReturnUserDetailsPasswordService() {
        UserDetailsPasswordService service = repository.getUserDetailsPasswordService();
        assertNotNull(service);
    }

    @Test
    void getUserDetailsService_LoadUserByUsername_WhenResponseIsNull_ShouldThrowUsernameNotFoundException() {
        String username = "testuser";
        when(communicator.executeRequest(any(AuthRequest.class))).thenReturn(Mono.empty());

        UserDetailsService service = repository.getUserDetailsService();
        
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(username);
        });
    }

    @Test
    void getUserDetailsService_LoadUserByUsername_WhenResponseStatusIsSuccess_ShouldReturnUser() {
        String username = "testuser";
        UserDetails expectedUser = User.withUsername(username).password("password").authorities("USER").build();
        AuthRequest authRequest = new AuthRequest("id", RequestType.GET_USER, username);
        authRequest.setResult(true, expectedUser);
        
        when(communicator.executeRequest(any(AuthRequest.class))).thenReturn(Mono.just(authRequest));

        UserDetailsService service = repository.getUserDetailsService();
        UserDetails result = service.loadUserByUsername(username);
        
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void getUserDetailsService_LoadUserByUsername_WhenResponseStatusIsError_ShouldThrowUsernameNotFoundException() {
        String username = "testuser";
        AuthRequest authRequest = new AuthRequest("id", RequestType.GET_USER, username);
        authRequest.setResult(false, "User not found");
        
        when(communicator.executeRequest(any(AuthRequest.class))).thenReturn(Mono.just(authRequest));

        UserDetailsService service = repository.getUserDetailsService();
        
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(username);
        });
    }

    @Test
    void getUserDetailsPasswordService_UpdatePassword_WhenResponseIsNull_ShouldThrowRuntimeException() {
        UserDetails userDetails = User.withUsername("testuser").password("oldPassword").authorities("USER").build();
        String newPassword = "newPassword";
        
        when(communicator.executeRequest(any(AuthRequest.class))).thenReturn(Mono.empty());

        UserDetailsPasswordService service = repository.getUserDetailsPasswordService();
        
        assertThrows(RuntimeException.class, () -> {
            service.updatePassword(userDetails, newPassword);
        });
    }

    @Test
    void getUserDetailsPasswordService_UpdatePassword_WhenResponseStatusIsSuccess_ShouldReturnUpdatedUser() {
        UserDetails userDetails = User.withUsername("testuser").password("oldPassword").authorities("USER").build();
        String newPassword = "newPassword";
        UserDetails expectedUser = User.withUsername("testuser").password(newPassword).authorities("USER").build();
        
        AuthRequest authRequest = new AuthRequest("id", RequestType.CHANGE_PASSWORD, expectedUser);
        authRequest.setResult(true, expectedUser);
        
        when(communicator.executeRequest(any(AuthRequest.class))).thenReturn(Mono.just(authRequest));

        UserDetailsPasswordService service = repository.getUserDetailsPasswordService();
        UserDetails result = service.updatePassword(userDetails, newPassword);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(newPassword, result.getPassword());
    }

    @Test
    void getUserDetailsPasswordService_UpdatePassword_WhenResponseStatusIsError_ShouldThrowRuntimeException() {
        UserDetails userDetails = User.withUsername("testuser").password("oldPassword").authorities("USER").build();
        String newPassword = "newPassword";
        
        AuthRequest authRequest = new AuthRequest("id", RequestType.CHANGE_PASSWORD, userDetails);
        authRequest.setResult(false, "Failed to update password");
        
        when(communicator.executeRequest(any(AuthRequest.class))).thenReturn(Mono.just(authRequest));

        UserDetailsPasswordService service = repository.getUserDetailsPasswordService();
        
        assertThrows(RuntimeException.class, () -> {
            service.updatePassword(userDetails, newPassword);
        });
    }
}
