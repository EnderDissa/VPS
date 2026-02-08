package com.example.warehouse.application.output;

import com.example.warehouse.infrastructure.persistence.entity.Storage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    boolean existsByName(String name);

    Page<Storage> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByNameContainingIgnoreCase(String name);

    Page<Storage> findAllBy(Pageable pageable);
}