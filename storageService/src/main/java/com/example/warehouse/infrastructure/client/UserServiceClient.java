package com.example.warehouse.infrastructure.client;

import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import com.example.warehouse.config.Fallback;
import com.example.warehouse.infrastructure.persistence.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "USERSERVICE", fallback = Fallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{id}")
    Mono<User> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/users/{id}/availability")
    Mono<Boolean> checkUserAvailability(
            @PathVariable("id") Long userId,
            @RequestParam("start") String start,
            @RequestParam("end") String end);

    @PostMapping("/internal/validate")
    Mono<UserDetailsEntity> checkUserAuth(@RequestBody String token);
}