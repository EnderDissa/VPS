package com.example.warehouse.dto;

import com.example.warehouse.entity.Keeping;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KeepingDTO(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "Storage ID is required")
        Long storageId,

        @NotNull(message = "Item ID is required")
        Long itemId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @Size(max = 100, message = "Shelf must not exceed 100 characters")
        String shelf,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime lastUpdated
) {

    public KeepingDTO {
        if (quantity == null) {
            quantity = 1;
        }
    }

    public KeepingDTO(Keeping keeping) {
        this(
                keeping != null ? keeping.getId() : null,
                keeping != null && keeping.getStorage() != null ? keeping.getStorage().getId() : null,
                keeping != null && keeping.getItem() != null ? keeping.getItem().getId() : null,
                keeping != null ? keeping.getQuantity() : 1,
                keeping != null ? keeping.getShelf() : null,
                keeping != null ? keeping.getLastUpdated() : null
        );
    }
}