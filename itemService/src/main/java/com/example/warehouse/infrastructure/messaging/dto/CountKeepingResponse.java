package com.example.warehouse.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountKeepingResponse {
    private String correlationId;
    private Long count;
    private boolean success;
    private String error;
}
