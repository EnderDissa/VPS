package com.example.warehouse.repository;

import com.example.warehouse.entity.Storage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    boolean existsByName(String name);

    Page<Storage> findByNameContainingIgnoreCase(String name, Pageable pageable);
}