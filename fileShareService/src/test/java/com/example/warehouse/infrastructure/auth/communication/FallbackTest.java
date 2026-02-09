package com.example.warehouse.infrastructure.auth.communication;

import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class FallbackTest {

    private final Fallback fallback = new Fallback();

    @Test
    void constructor_ShouldCreateInstance() {
        Fallback fallback = new Fallback();
        assertNotNull(fallback);
    }

    @Test
    void checkUserAuth_ShouldReturnErrorMono() {
        String token = "test-token";
        
        Mono<UserDetailsEntity> result = fallback.checkUserAuth(token);
        
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void checkUserAuth_ErrorMessage_ShouldContainFallbackMessage() {
        String token = "test-token";
        
        Mono<UserDetailsEntity> result = fallback.checkUserAuth(token);
        
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().contains("UserService is currently unavailable"))
                .verify();
    }
}
