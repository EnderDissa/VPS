package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.Keeping;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KeepingService {
    Mono<Keeping> create(Keeping keeping);
    Mono<Keeping> getById(Long id);
    Mono<Void> update(Long id, Keeping keeping);
    Mono<Void> delete(Long id);
    Flux<Keeping> findKeepingsByFilters(Long storageId, Long itemId, Pageable pageable);
    Mono<Long> countKeepingsByFilters(Long storageId, Long itemId);
}