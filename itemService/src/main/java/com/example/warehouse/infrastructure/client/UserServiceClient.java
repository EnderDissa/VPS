package com.example.warehouse.infrastructure.client;

import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import com.example.warehouse.infrastructure.config.Fallback;
import com.example.warehouse.infrastructure.persistence.entity.User;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
<<<<<<< HEAD
=======
import org.springframework.web.bind.annotation.RequestHeader;
>>>>>>> ab62fea2a9f7560dc90c80d67cf022f04239a1d8
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
@ReactiveFeignClient(name = "USERSERVICE", fallback = Fallback.class)
public interface UserServiceClient {

<<<<<<< HEAD
    @GetMapping("/api/v1/users/{id}")
    Mono<User> getUserById(@RequestParam("id") Long id);
=======
    @GetMapping("/api/v1/users/existsId/{id}")
    Mono<Long> getUserById(@RequestParam("id") Long id, @RequestHeader("Authorization") String authorization);
>>>>>>> ab62fea2a9f7560dc90c80d67cf022f04239a1d8

    @PostMapping("/internal/validate")
    Mono<UserDetailsEntity> checkUserAuth(@RequestBody String token);
}