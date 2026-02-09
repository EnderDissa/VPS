package com.example.warehouse.infrastructure.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoteAuthenticationManagerTest {

    @Mock
    private CrossServiceUserRepository userRepository;

    @Mock
    private UserDetailsService userDetailsService;

    private RemoteAuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        authenticationManager = new RemoteAuthenticationManager();
        // Using reflection to set the private field for testing
        setPrivateField("userRepository", userRepository);
    }

    @Test
    void authenticate_WhenAuthenticationPrincipalIsNull_ShouldReturnNull() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);

        Authentication result = authenticationManager.authenticate(authentication);

        assertNull(result);
    }

    @Test
    void authenticate_WhenUserNotFound_ShouldReturnNull() {
        String username = "testuser";
        String password = "password";
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        
        when(userRepository.getUserDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException("User not found"));

        Authentication result = authenticationManager.authenticate(authentication);

        assertNull(result);
    }

    @Test
    void authenticate_WhenBadCredentials_ShouldThrowBadCredentialsException() {
        String username = "testuser";
        String password = "password";
        UserDetails user = User.withUsername(username).password(password).authorities("USER").build();
        UserDetails differentUser = User.withUsername(username).password("differentPassword").authorities("USER").build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        
        when(userRepository.getUserDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(differentUser);

        assertThrows(BadCredentialsException.class, () -> {
            authenticationManager.authenticate(authentication);
        });
    }

    @Test
    void constructor_ShouldCreateInstance() {
        RemoteAuthenticationManager manager = new RemoteAuthenticationManager();
        assertNotNull(manager);
    }

    // Helper method to set private fields using reflection for testing
    private void setPrivateField(String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = RemoteAuthenticationManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(authenticationManager, value);
        } catch (Exception e) {
            fail("Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }
}
