package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ItemService {
    Mono<Item> create(Item item);
    Mono<Item> getById(Long id);
    Mono<Void> update(Long id, Item item);
    Mono<Void> delete(Long id);
    Flux<Item> findItemsByFilters(ItemType type, ItemCondition condition, Pageable pageable);
    Mono<Long> countItemsByFilters(ItemType type, ItemCondition condition);
    Flux<Item> findAvailable(LocalDateTime from, LocalDateTime to, Long storageId,
                             ItemType type, ItemCondition condition, Long cursor, int limit);
}