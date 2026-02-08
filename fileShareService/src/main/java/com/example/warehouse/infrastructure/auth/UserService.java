package com.example.warehouse.infrastructure.auth;

import com.example.warehouse.infrastructure.auth.communication.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserServiceClient userServiceClient;
    
    @Value("${jwt.secret:mySecretKey}") // Default secret key, should be configured properly
    private String jwtSecret;

    @Autowired
    public UserService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public Mono<Long> getUserIdFromToken(String token) {
        // In a real implementation, this would decode the JWT token to extract the user ID.
        // For now, we'll return a placeholder since we don't have access to the actual JWT structure.
        // This should be implemented properly based on how your JWT tokens are structured.
        return Mono.just(1L); // Placeholder - should be replaced with actual implementation
    }
}
