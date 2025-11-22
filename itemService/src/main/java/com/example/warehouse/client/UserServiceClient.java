package com.example.warehouse.client;

import com.example.warehouse.config.Fallback;
import com.example.warehouse.entity.User;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
@ReactiveFeignClient(name = "USERSERVICE", fallback = Fallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{id}")
    Mono<User> getUserById(@RequestParam("id") Long id);
}