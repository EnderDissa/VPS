package com.example.warehouse.infrastructure.config;

import com.example.warehouse.infrastructure.client.UserServiceClient;
import com.example.warehouse.infrastructure.persistence.entity.User;


import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Fallback implements UserServiceClient {

    private String cause;

    public Fallback() {
    }

    @Override
    public Mono<User> getUserById(Long id) {
        log.warn("Fallback: Returning a default user or error object for id: {}", id);

        return Mono.error(new RuntimeException("UserService is currently unavailable (fallback). Original cause: " + cause, null));
    }
}