package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel; // Assuming this enum exists
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserStorageAccessService {
    Mono<UserStorageAccess> create(UserStorageAccess userStorageAccess);
    Mono<UserStorageAccess> getById(Long id);
    Mono<Void> update(Long id, UserStorageAccess userStorageAccess);
    Mono<Void> delete(Long id);
    Flux<UserStorageAccess> findUserStorageAccessesByFilters(Long userId, Long storageId, String accessLevel, Boolean active, Pageable pageable); // Use String
    Mono<Long> countUserStorageAccessesByFilters(Long userId, Long storageId, String accessLevel, Boolean active); // Use String
    Mono<UserStorageAccess> findByUserAndStorage(Long userId, Long storageId);
    Mono<Boolean> hasAccess(Long userId, Long storageId, AccessLevel requiredLevel);
    Mono<UserStorageAccess> deactivate(Long id);
    Mono<UserStorageAccess> activate(Long id);
    Flux<UserStorageAccess> findByUser(Long userId);
    Flux<UserStorageAccess> findByStorage(Long storageId);
    Flux<UserStorageAccess> findExpiredAccesses();
    Mono<Void> deactivateExpiredAccesses();
    Mono<Long> countActiveAccessesByUser(Long userId);
    Mono<Long> countActiveAccessesByStorage(Long storageId);
}