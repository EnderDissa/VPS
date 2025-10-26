package com.example.warehouse.dto.UserDTO;

import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String firstName,
        String secondName,
        String lastName,
        RoleType role,
        String email,
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