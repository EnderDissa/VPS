package com.example.warehouse.application.input.interfaces;

import com.example.warehouse.infrastructure.persistence.entity.Transportation;
import com.example.warehouse.domain.enumeration.TransportStatus;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface TransportationService {
    Mono<Transportation> create(Transportation transportation);
    Mono<Transportation> getById(Long id);
    Mono<Transportation> update(Long id, Transportation transportation);
    Mono<Void> delete(Long id);
    Mono<Page<Transportation>> findPage(int page, int size, TransportStatus status, Long itemId,
                                        Long fromStorageId, Long toStorageId);
}
