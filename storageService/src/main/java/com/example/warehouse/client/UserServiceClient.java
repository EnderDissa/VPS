package com.example.warehouse.client;

import com.example.warehouse.config.Fallback;
import com.example.warehouse.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "USER-SERVICE", fallback = Fallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{id}")
    Mono<User> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/users/{id}/availability")
    Mono<Boolean> checkUserAvailability(
            @PathVariable("id") Long userId,
            @RequestParam("start") String start,
            @RequestParam("end") String end);
}