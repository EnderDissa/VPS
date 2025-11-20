package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemService {
    Item create(Item item);
    Item getById(Long id);
    void update(Long id, Item item);
    void delete(Long id);
    Page<Item> findPage(int page, int size, ItemType type, ItemCondition condition);
    List<Item> findAvailable(LocalDateTime from, LocalDateTime to, Long storageId,
                                ItemType type, ItemCondition condition, Long cursor, int limit);
}
