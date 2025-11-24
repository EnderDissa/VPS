package com.example.warehouse.client;

import com.example.warehouse.config.Fallback;
import com.example.warehouse.entity.Item;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "ITEMSERVICE", fallback = Fallback.class)
public interface ItemServiceClient {

    @GetMapping("/api/v1/items/{id}")
    Mono<Item> getItemById(@PathVariable("id") Long id);
}
