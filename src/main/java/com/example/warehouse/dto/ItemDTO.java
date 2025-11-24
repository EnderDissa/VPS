package com.example.warehouse.dto;

import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ItemDTO(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
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

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(example = "2025-11-24T03:29:34", accessMode = Schema.AccessMode.READ_ONLY)
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