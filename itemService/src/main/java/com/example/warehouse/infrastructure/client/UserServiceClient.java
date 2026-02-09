package com.example.warehouse.infrastructure.client;

import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import com.example.warehouse.infrastructure.config.Fallback;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
@ReactiveFeignClient(name = "USERSERVICE", fallback = Fallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/existsId/{id}")
    Mono<Long> getUserById(@RequestParam("id") Long id, @RequestHeader("Authorization") String authorization);

    @PostMapping("/internal/validate")
    Mono<UserDetailsEntity> checkUserAuth(@RequestBody String token);
}