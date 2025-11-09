package com.example.warehouse.repository;

import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStorageAccessRepository extends JpaRepository<UserStorageAccess, Long> {

    boolean existsByUserIdAndStorageId(Long userId, Long storageId);

    boolean existsByUserIdAndStorageIdAndIdNot(Long userId, Long storageId, Long id);

    Optional<UserStorageAccess> findByUserIdAndStorageId(Long userId, Long storageId);

    Page<UserStorageAccess> findByUserId(Long userId, Pageable pageable);

    Page<UserStorageAccess> findByStorageId(Long storageId, Pageable pageable);

    Page<UserStorageAccess> findByAccessLevel(AccessLevel accessLevel, Pageable pageable);

    Page<UserStorageAccess> findByIsActive(Boolean isActive, Pageable pageable);

    Page<UserStorageAccess> findByUserIdAndStorageId(Long userId, Long storageId, Pageable pageable);

    Page<UserStorageAccess> findByUserIdAndAccessLevel(Long userId, AccessLevel accessLevel, Pageable pageable);

    Page<UserStorageAccess> findByUserIdAndIsActive(Long userId, Boolean isActive, Pageable pageable);

    Page<UserStorageAccess> findByStorageIdAndAccessLevel(Long storageId, AccessLevel accessLevel, Pageable pageable);

    Page<UserStorageAccess> findByStorageIdAndIsActive(Long storageId, Boolean isActive, Pageable pageable);

    Page<UserStorageAccess> findByAccessLevelAndIsActive(AccessLevel accessLevel, Boolean isActive, Pageable pageable);

    Page<UserStorageAccess> findByUserIdAndStorageIdAndAccessLevel(Long userId, Long storageId, AccessLevel accessLevel, Pageable pageable);

    Page<UserStorageAccess> findByUserIdAndStorageIdAndIsActive(Long userId, Long storageId, Boolean isActive, Pageable pageable);

    Page<UserStorageAccess> findByUserIdAndAccessLevelAndIsActive(Long userId, AccessLevel accessLevel, Boolean isActive, Pageable pageable);

    Page<UserStorageAccess> findByStorageIdAndAccessLevelAndIsActive(Long storageId, AccessLevel accessLevel, Boolean isActive, Pageable pageable);

    Page<UserStorageAccess> findByUserIdAndStorageIdAndAccessLevelAndIsActive(
            Long userId, Long storageId, AccessLevel accessLevel, Boolean isActive, Pageable pageable);

    List<UserStorageAccess> findByUserId(Long userId);

    List<UserStorageAccess> findByStorageId(Long storageId);

    @Query("SELECT usa FROM UserStorageAccess usa WHERE usa.expiresAt < :now AND usa.isActive = true")
    List<UserStorageAccess> findExpiredAccesses(@Param("now") LocalDateTime now);

    long countByUserIdAndIsActive(Long userId, Boolean isActive);

    long countByStorageIdAndIsActive(Long storageId, Boolean isActive);
}