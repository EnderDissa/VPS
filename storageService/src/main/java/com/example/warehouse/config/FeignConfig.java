package com.example.warehouse.config;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.cloud2.ReactiveFeignCircuitBreakerFactory;

@Configuration
@EnableFeignClients(basePackages = "com.example.warehouse.client")
public class FeignConfig {

    @Bean
    public ReactiveFeignCircuitBreakerFactory reactiveFeignCircuitBreakerFactory(
            ReactiveCircuitBreakerFactory<?, ?> springCloudCircuitBreakerFactory
    ) {
        return springCloudCircuitBreakerFactory::create;
    }
}
