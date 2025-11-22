package com.example.warehouse.config;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactivefeign.cloud2.ReactiveFeignCircuitBreakerFactory;

// Example Configuration Class
@Configuration
public class FeignConfig {

    @Bean
    public ReactiveFeignCircuitBreakerFactory reactiveFeignCircuitBreakerFactory(
            ReactiveCircuitBreakerFactory springCloudCircuitBreakerFactory // This is injected by Spring
    ) {
        return springCloudCircuitBreakerFactory::create;
    }

}