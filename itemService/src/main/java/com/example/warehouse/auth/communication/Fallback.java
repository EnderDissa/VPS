package com.example.warehouse.auth.communication;


import com.example.warehouse.auth.UserDetailsEntity;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Fallback implements UserServiceClient {

    public Fallback() {
    }

    @Override
    public Mono<UserDetailsEntity> checkUserAuth(String token) {
        log.warn("Fallback: Returning error for user: {}", token);
        return Mono.error(new RuntimeException("UserService is currently unavailable (fallback)."));
    }
}