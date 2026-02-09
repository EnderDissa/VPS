package com.example.warehouse.infrastructure.messaging.dto;

import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.domain.enumeration.ItemType;

import com.example.warehouse.infrastructure.persistence.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemData {
    private Long id;
    private String name;
    private ItemType type;
    private ItemCondition condition;
    private String serialNumber;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemData fromEntity(Item item) {
        return ItemData.builder()
                .id(item.getId())
                .name(item.getName())
                .type(item.getType())
                .condition(item.getCondition())
                .serialNumber(item.getSerialNumber())
                .description(item.getDescription())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
