package com.example.warehouse.config;

import com.example.warehouse.client.StorageServiceClient;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.entity.User;

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