package com.example.warehouse.dto;

import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemDTO {

    public ItemDTO(Item item) {
        if (item == null) return;
        this.id = item.getId();
        this.name = item.getName();
        this.type = item.getType();
        this.condition = item.getCondition();
        this.serialNumber = item.getSerialNumber();
        this.description = item.getDescription();
        this.createdAt = item.getCreatedAt();
    }

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Type is required")
    private ItemType type;

    @NotNull(message = "Condition is required")
    private ItemCondition condition;

    @Size(max = 100, message = "Serial number must not exceed 100 characters")
    private String serialNumber;

    private String description;

    private LocalDateTime createdAt;
}