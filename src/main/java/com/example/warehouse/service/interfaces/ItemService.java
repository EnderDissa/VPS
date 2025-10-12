package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemService {
    ItemDTO create(ItemDTO dto);
    ItemDTO getById(Long id);
    void update(Long id, ItemDTO dto);
    void delete(Long id);
    Page<ItemDTO> findPage(int page, int size, ItemType type, ItemCondition condition);
    List<ItemDTO> findAvailable(LocalDateTime from, LocalDateTime to, Long storageId,
                                ItemType type, ItemCondition condition, Long cursor, int limit);
}
