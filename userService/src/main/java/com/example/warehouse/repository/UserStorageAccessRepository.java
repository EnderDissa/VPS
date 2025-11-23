package com.example.warehouse.repository;

import com.example.warehouse.entity.UserStorageAccess;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface UserStorageAccessRepository extends ReactiveSortingRepository<UserStorageAccess, Long>, ReactiveCrudRepository<UserStorageAccess, Long> {

    @Query("SELECT * FROM user_storage_access WHERE user_id = :userId AND storage_id = :storageId")
    Mono<UserStorageAccess> findByUserIdAndStorageId(Long userId, Long storageId);

    @Query("SELECT * FROM user_storage_access WHERE user_id = :userId")
    Flux<UserStorageAccess> findByUserId(Long userId);

    @Query("SELECT * FROM user_storage_access WHERE storage_id = :storageId")
    Flux<UserStorageAccess> findByStorageId(Long storageId);

    @Query("SELECT * FROM user_storage_access WHERE expires_at IS NOT NULL AND expires_at < :now AND is_active = true")
    Flux<UserStorageAccess> findExpiredAccesses(LocalDateTime now);

    @Query("SELECT EXISTS(SELECT 1 FROM user_storage_access WHERE user_id = :userId AND storage_id = :storageId AND id != :id)")
    Mono<Boolean> existsByUserIdAndStorageIdAndIdNot(Long userId, Long storageId, Long id);

    @Query("SELECT * FROM user_storage_access WHERE (:userId IS NULL OR user_id = :userId) " +
            "AND (:storageId IS NULL OR storage_id = :storageId) " +
            "AND (:accessLevel IS NULL OR access_level = :accessLevel) " +
            "AND (:active IS NULL OR is_active = :active) " +
            "LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}")
    Flux<UserStorageAccess> findByUserIdAndStorageIdAndAccessLevel(Long userId, Long storageId, String accessLevel, Boolean active, Pageable pageable);

    @Query("SELECT COUNT(*) FROM user_storage_access WHERE (:userId IS NULL OR user_id = :userId) " +
            "AND (:storageId IS NULL OR storage_id = :storageId) " +
            "AND (:accessLevel IS NULL OR access_level = :accessLevel) " +
            "AND (:active IS NULL OR is_active = :active)")
    Mono<Long> countByFilters(Long userId, Long storageId, String accessLevel, Boolean active);

    @Query("SELECT COUNT(*) FROM user_storage_access WHERE user_id = :userId AND is_active = :isActive")
    Mono<Long> countByUserIdAndIsActive(Long userId, Boolean isActive);

    @Query("SELECT COUNT(*) FROM user_storage_access WHERE storage_id = :storageId AND is_active = :isActive")
    Mono<Long> countByStorageIdAndIsActive(Long storageId, Boolean isActive);

    @Query("SELECT COUNT(*) FROM user_storage_access")
    Mono<Long> count();
}