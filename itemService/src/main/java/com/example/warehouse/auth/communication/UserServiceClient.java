package com.example.warehouse.auth.communication;


import com.example.warehouse.auth.UserDetailsEntity;

import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "USERSERVICE", fallback = Fallback.class)
public interface UserServiceClient {

    @PostMapping("/internal/validate")
    Mono<UserDetailsEntity> checkUserAuth(@RequestBody String token);

}