package com.example.warehouse.repository;

import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    Page<Item> findByType(ItemType type, Pageable pageable);

    Page<Item> findByCondition(ItemCondition condition, Pageable pageable);

    Page<Item> findByTypeAndCondition(ItemType type, ItemCondition condition, Pageable pageable);

    Page<Item> findByIdGreaterThan(Long id, Pageable pageable);

    Page<Item> findByIdGreaterThanAndType(Long id, ItemType type, Pageable pageable);

    Page<Item> findByIdGreaterThanAndCondition(Long id, ItemCondition condition, Pageable pageable);

    Page<Item> findByIdGreaterThanAndTypeAndCondition(Long id, ItemType type, ItemCondition condition, Pageable pageable);
}