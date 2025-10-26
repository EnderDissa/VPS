package com.example.warehouse.dto;

import com.example.warehouse.entity.Storage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

public record StorageDTO(
        Long id,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @PositiveOrZero(message = "Capacity must be positive or zero")
        Integer capacity,

        LocalDateTime createdAt
) {}