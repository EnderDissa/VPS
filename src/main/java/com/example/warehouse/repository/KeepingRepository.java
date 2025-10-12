package com.example.warehouse.repository;

import com.example.warehouse.entity.Keeping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeepingRepository extends JpaRepository<Keeping, Long> {

    boolean existsByStorageIdAndItemId(Long storageId, Long itemId);

    boolean existsByStorageIdAndItemIdAndIdNot(Long storageId, Long itemId, Long id);

    Optional<Keeping> findByStorageIdAndItemId(Long storageId, Long itemId);

    Page<Keeping> findByStorageId(Long storageId, Pageable pageable);

    Page<Keeping> findByItemId(Long itemId, Pageable pageable);

    Page<Keeping> findByStorageIdAndItemId(Long storageId, Long itemId, Pageable pageable);

    long countByStorageId(Long storageId);

    long countByItemId(Long itemId);

    @Query("SELECT SUM(k.quantity) FROM Keeping k WHERE k.storage.id = :storageId")
    Integer getTotalQuantityInStorage(@Param("storageId") Long storageId);
}