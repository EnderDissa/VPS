package com.example.warehouse.dto;

import com.example.warehouse.entity.Storage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record StorageDTO(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @PositiveOrZero(message = "Capacity must be positive or zero")
        Integer capacity,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
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