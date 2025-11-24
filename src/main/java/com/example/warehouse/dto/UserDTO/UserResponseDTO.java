package com.example.warehouse.dto.UserDTO;

import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserResponseDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String secondName,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String lastName,

        @NotNull(message = "Role is required")
        RoleType role,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime createdAt
) {

    public UserResponseDTO(User user) {
        this(
                user != null ? user.getId() : null,
                user != null ? user.getFirstName() : null,
                user != null ? user.getSecondName() : null,
                user != null ? user.getLastName() : null,
                user != null ? user.getRole() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getCreatedAt() : null
        );
    }
}