package com.mastik.gateway.auth.communication;

import com.mastik.gateway.auth.AuthRequest;
import com.mastik.gateway.auth.UserDetailsEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "USERSERVICE", fallback = Fallback.class)
public interface UserServiceClient {

    @GetMapping("/internal/get")
    Mono<UserDetailsEntity> checkUserExists(@RequestBody String email);

    @GetMapping("/internal/validate")
    Mono<UserDetailsEntity> checkUserAuth(@RequestBody String email, @RequestBody String password);

}