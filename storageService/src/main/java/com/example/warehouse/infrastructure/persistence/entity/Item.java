package com.example.warehouse.infrastructure.persistence.entity;

import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.domain.enumeration.ItemType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private Long id;

    private String name;

    private ItemType type;

    private ItemCondition condition;

    private String serialNumber;

    private String description;

    private LocalDateTime createdAt;
}
