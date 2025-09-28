package com.example.warehouse.dto;

import com.example.warehouse.entity.Keeping;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeepingDTO {

    public KeepingDTO(Keeping keeping) {
        if (keeping == null) return;
        this.id = keeping.getId();
        this.storageId = keeping.getStorage() != null ? keeping.getStorage().getId() : null;
        this.itemId = keeping.getItem() != null ? keeping.getItem().getId() : null;
        this.quantity = keeping.getQuantity();
        this.shelf = keeping.getShelf();
        this.lastUpdated = keeping.getLastUpdated();
    }

    private Long id;

    @NotNull(message = "Storage ID is required")
    private Long storageId;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;

    @Size(max = 100, message = "Shelf must not exceed 100 characters")
    private String shelf;

    private LocalDateTime lastUpdated;
}