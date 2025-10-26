package com.example.warehouse.dto;

import com.example.warehouse.entity.Storage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public record StorageDTO(
        Long id,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @PositiveOrZero(message = "Capacity must be positive or zero")
        Integer capacity,

        LocalDateTime createdAt
) {

        public StorageDTO(Storage storage) {
                this(
                        storage != null ? storage.getId() : null,
                        storage != null ? storage.getName() : null,
                        storage != null ? storage.getAddress() : null,
                        storage != null ? storage.getCapacity() : null,
                        storage != null ? storage.getCreatedAt() : null
                );
        }
}