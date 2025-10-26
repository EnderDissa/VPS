package com.example.warehouse.dto.UserDTO;

import com.example.warehouse.enumeration.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UserRequestDTO(
        Long id,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String secondName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @NotNull(message = "Role is required")
        RoleType role,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        LocalDateTime createdAt
) {}