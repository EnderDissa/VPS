package com.example.warehouse.config;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactivefeign.cloud2.ReactiveFeignCircuitBreakerFactory;

@Configuration
public class FeignConfig {

    @Bean
    public ReactiveFeignCircuitBreakerFactory reactiveFeignCircuitBreakerFactory(
            ReactiveCircuitBreakerFactory springCloudCircuitBreakerFactory
    ) {
        return springCloudCircuitBreakerFactory::create;
    }

}