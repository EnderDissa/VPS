package com.example.warehouse.infrastructure.auth;

import com.example.warehouse.infrastructure.auth.communication.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserServiceClient userServiceClient;
    
    @Value("${jwt.secret:mySecretKey}") 
    private String jwtSecret;

    @Autowired
    public UserService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public Mono<Long> getUserIdFromToken(String token) {
        
        
        
        return Mono.just(1L); 
    }
}
