package com.example.warehouse.infrastructure.auth.jwt;

import com.example.warehouse.infrastructure.auth.CrossServiceUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTAuthFilterTest {

    @Mock
    private CrossServiceUserRepository userRepository;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private WebFilterChain chain;

    @Mock
    private ServerHttpRequest request;

    private JWTAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JWTAuthFilter(userRepository);
    }

    @Test
    void constructor_ShouldInitializeWithUserRepository() {
        JWTAuthFilter filter = new JWTAuthFilter(userRepository);
        assertNotNull(filter);
    }

    @Test
    void filter_WhenNoAuthorizationHeader_ShouldContinueChain() {
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain).filter(exchange);
    }

    @Test
    void filter_WhenValidBearerTokenAndUserFound_ShouldSetAuthenticationAndContinueChain() {
        String jwtToken = "valid-jwt-token";
        String bearerToken = "Bearer " + jwtToken;
        UserDetails userDetails = User.withUsername("testuser").password("password").authorities("USER").build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearerToken);
        
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(userRepository.getUserDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(jwtToken)).thenReturn(userDetails);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_WhenExceptionDuringProcessing_ShouldContinueChain() {
        String bearerToken = "Bearer valid-token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearerToken);
        
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getRequest()).thenReturn(request);
        when(userRepository.getUserDetailsService()).thenThrow(new RuntimeException("Service error"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain).filter(exchange);
    }

    @Test
    void buildAuthentication_ShouldCreateUsernamePasswordAuthenticationToken() {
        UserDetails userDetails = User.withUsername("testuser").password("password").authorities("USER").build();
        
        // Using reflection to test private method
        try {
            java.lang.reflect.Method method = JWTAuthFilter.class.getDeclaredMethod("buildAuthentication", UserDetails.class);
            method.setAccessible(true);
            
            Object result = method.invoke(jwtAuthFilter, userDetails);
            
            assertInstanceOf(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class, result);
            
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = 
                (org.springframework.security.authentication.UsernamePasswordAuthenticationToken) result;
                
            assertEquals(userDetails, authToken.getPrincipal());
            assertEquals("43dd32434d2d233", authToken.getCredentials());
            assertEquals(1, authToken.getAuthorities().size());
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }
}
