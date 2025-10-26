package com.example.warehouse.dto;

import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public record ItemDTO(
        Long id,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @NotNull(message = "Type is required")
        ItemType type,

        @NotNull(message = "Condition is required")
        ItemCondition condition,

        @Size(max = 100, message = "Serial number must not exceed 100 characters")
        String serialNumber,

        String description,

        LocalDateTime createdAt
) {

    public ItemDTO(Item item) {
        this(
                item != null ? item.getId() : null,
                item != null ? item.getName() : null,
                item != null ? item.getType() : null,
                item != null ? item.getCondition() : null,
                item != null ? item.getSerialNumber() : null,
                item != null ? item.getDescription() : null,
                item != null ? item.getCreatedAt() : null
        );
    }
}