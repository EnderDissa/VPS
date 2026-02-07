package com.example.warehouse.application.ports.output;

import com.example.warehouse.infrastructure.persistence.entity.Item;
import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.domain.enumeration.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    boolean existsBySerialNumber(String serialNumber);

    Page<Item> findByType(ItemType type, Pageable pageable);

    Page<Item> findByCondition(ItemCondition condition, Pageable pageable);

    Page<Item> findByTypeAndCondition(ItemType type, ItemCondition condition, Pageable pageable);

    Page<Item> findByIdGreaterThan(Long id, Pageable pageable);

    Page<Item> findByIdGreaterThanAndType(Long id, ItemType type, Pageable pageable);

    Page<Item> findByIdGreaterThanAndCondition(Long id, ItemCondition condition, Pageable pageable);

    Page<Item> findByIdGreaterThanAndTypeAndCondition(Long id, ItemType type, ItemCondition condition, Pageable pageable);
}