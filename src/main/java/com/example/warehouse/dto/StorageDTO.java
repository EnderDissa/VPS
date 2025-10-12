package com.example.warehouse.dto;

import com.example.warehouse.entity.Storage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StorageDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @PositiveOrZero(message = "Capacity must be positive or zero")
    private Integer capacity;

    private LocalDateTime createdAt;
}