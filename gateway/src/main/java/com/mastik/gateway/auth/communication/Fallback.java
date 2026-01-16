package com.mastik.gateway.auth.communication;

import com.mastik.gateway.auth.AuthRequest;
import com.mastik.gateway.auth.UserDetailsEntity;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Fallback implements UserServiceClient {

    public Fallback() {
    }

    @Override
    public Mono<UserDetailsEntity> checkUserAuth(String email, String password) {
        log.warn("Fallback: Returning error for user: {}", email);
        return Mono.error(new RuntimeException("ItemService is currently unavailable (fallback)."));
    }
}