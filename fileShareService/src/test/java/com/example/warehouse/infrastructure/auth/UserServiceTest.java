package com.example.warehouse.infrastructure.auth;

import com.example.warehouse.infrastructure.auth.communication.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userServiceClient);
    }

    @Test
    void getUserIdFromToken_ShouldReturnPlaceholderUserId() {
        String token = "test-token";
        
        Mono<Long> result = userService.getUserIdFromToken(token);
        
        StepVerifier.create(result)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void constructor_ShouldInitializeWithUserServiceClient() {
        UserService service = new UserService(userServiceClient);
        assertNotNull(service);
    }
}
