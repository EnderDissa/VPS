package com.example.warehouse.application.input.interfaces;

import com.example.warehouse.infrastructure.persistence.entity.Storage;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface StorageService {

    Mono<Storage> create(Storage storage);

    Mono<Storage> getById(Long id);

    Mono<Storage> update(Long id, Storage storage);

    Mono<Void> delete(Long id);

    Mono<Page<Storage>> findPage(int page, int size, String nameLike);
}
