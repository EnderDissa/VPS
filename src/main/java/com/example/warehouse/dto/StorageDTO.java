package com.example.warehouse.dto;

import com.example.warehouse.entity.Storage;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
public class StorageDTO {

    public StorageDTO(Storage storage) {
        if (storage == null) return;
        this.id = storage.getId();
        this.name = storage.getName();
        this.address = storage.getAddress();
        this.capacity = storage.getCapacity();
        this.createdAt = storage.getCreatedAt();
    }

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @PositiveOrZero(message = "Capacity must be positive or zero")
    private Integer capacity;

    private LocalDateTime createdAt;
}