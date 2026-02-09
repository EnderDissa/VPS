package com.example.warehouse.infrastructure.auth;

import com.example.warehouse.infrastructure.auth.jwt.JWTAuthFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CrossServiceUserRepository userRepository;

    @Mock
    private JWTAuthFilter jwtFilter;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        // Using reflection to set private fields for testing
        setPrivateField("userRepository", userRepository);
        setPrivateField("jwtFilter", jwtFilter);
    }

    @Test
    void securityWebFilterChain_ShouldCreateFilterChain() throws Exception {
        org.springframework.security.config.web.server.ServerHttpSecurity http = 
            org.springframework.security.config.web.server.ServerHttpSecurity.http();

        SecurityWebFilterChain filterChain = securityConfig.securityWebFilterChain(http);

        assertNotNull(filterChain);
    }

    @Test
    void daoAuthenticationProvider_ShouldCreateAuthenticationProvider() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        AuthenticationProvider provider = securityConfig.daoAuthenticationProvider(passwordEncoder);
        
        assertNotNull(provider);
    }

    @Test
    void userDetailsService_ShouldReturnUserDetailsService() {
        when(userRepository.getUserDetailsService()).thenReturn(mock(UserDetailsService.class));
        
        UserDetailsService service = securityConfig.userDetailsService(mock(PasswordEncoder.class));
        
        assertNotNull(service);
    }

    @Test
    void passwordEncoder_ShouldCreateBCryptPasswordEncoder() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();
        
        assertNotNull(encoder);
    }

    @Test
    void constructor_ShouldCreateInstance() {
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config);
    }

    // Helper method to set private fields using reflection for testing
    private void setPrivateField(String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = SecurityConfig.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(securityConfig, value);
        } catch (Exception e) {
            fail("Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }
}
