package com.example.warehouse.repository;

import com.example.warehouse.entity.Storage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StorageRepository extends ReactiveCrudRepository<Storage, Long> {

    Mono<Boolean> existsByName(String name);

    Flux<Storage> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Mono<Long> countByNameContainingIgnoreCase(String name);

    Flux<Storage> findAllBy(Pageable pageable);

    @Query("SELECT COUNT(k) FROM Keeping k WHERE k.storage.id = :storageId")
    Mono<Long> countKeepingsByStorageId(@Param("storageId") Long storageId);
}