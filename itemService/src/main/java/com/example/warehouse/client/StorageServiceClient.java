package com.example.warehouse.client;

import com.example.warehouse.entity.Storage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "STORAGESERVICE")
public interface StorageServiceClient {

    @GetMapping("/api/storage/{id}")
    Mono<Storage> getById(@RequestParam("id") Long id);
}