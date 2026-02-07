package com.example.warehouse.config;

import com.example.warehouse.infrastructure.client.StorageServiceClient;
import com.example.warehouse.infrastructure.persistence.entity.Storage;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Fallback implements StorageServiceClient {

    public Fallback() {
    }

    @Override
    public Mono<Storage> getById(Long id) {
        log.warn("Fallback: Returning a default storage or error object for id: {}", id);

        return Mono.error(new RuntimeException("StorageService is currently unavailable (fallback)."));
    }
}