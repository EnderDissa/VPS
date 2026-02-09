package com.example.warehouse.infrastructure.messaging.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String correlationId; // для связки запрос-ответ
    private Long userId;
}
