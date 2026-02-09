package com.example.warehouse.infrastructure.messaging.dto;

import com.example.warehouse.infrastructure.persistence.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private String correlationId;
    private Item item;      // ваша сущность из БД
    private boolean success;
    private String error;
}
