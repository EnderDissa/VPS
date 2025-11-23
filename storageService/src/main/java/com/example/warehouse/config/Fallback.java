package com.example.warehouse.config;

import com.example.warehouse.client.ItemServiceClient;
import com.example.warehouse.client.UserServiceClient;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Fallback implements UserServiceClient, ItemServiceClient {

    private String cause;

    public Fallback() {
    }

    public Fallback(String cause) {
        this.cause = cause;
    }

    @Override
    public Mono<User> getUserById(Long id) {
        log.warn("Fallback: Returning error for user id: {}", id);
        return Mono.error(new RuntimeException("UserService is currently unavailable (fallback). Original cause: " + cause));
    }

    @Override
    public Mono<Boolean> checkUserAvailability(Long userId, String start, String end) {
        log.warn("Fallback: Returning default availability for user id: {}", userId);
        // В fallback считаем пользователя доступным
        return Mono.just(true);
    }

    @Override
    public Mono<Item> getItemById(Long id) {
        log.warn("Fallback: Returning error for item id: {}", id);
        return Mono.error(new RuntimeException("ItemService is currently unavailable (fallback). Original cause: " + cause));
    }
}
