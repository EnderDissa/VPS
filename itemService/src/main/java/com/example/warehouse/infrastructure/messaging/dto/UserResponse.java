package com.example.warehouse.infrastructure.messaging.dto;


import com.example.warehouse.infrastructure.persistence.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String correlationId;
    private User user;      // ваша сущность из БД
    private boolean success;
    private String error;
}
