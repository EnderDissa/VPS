package com.example.warehouse.dto;

import com.example.warehouse.entity.Keeping;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record KeepingDTO(
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