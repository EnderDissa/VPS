package com.example.warehouse.infrastructure.messaging.dto;

import com.example.warehouse.infrastructure.persistence.entity.Storage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageResponse {
    private String correlationId;
    private Storage storage;
    private boolean success;
    private String error;
}
