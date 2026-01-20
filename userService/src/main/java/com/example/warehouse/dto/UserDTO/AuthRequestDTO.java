package com.example.warehouse.dto.UserDTO;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AuthRequestDTO(
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}
