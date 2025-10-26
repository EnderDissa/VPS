package com.example.warehouse.dto;

import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UserStorageAccessDTO(
        Long id,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Storage ID is required")
        Long storageId,

        @NotNull(message = "Access level is required")
        AccessLevel accessLevel,

        @NotNull(message = "Granted by user ID is required")
        Long grantedById,

        LocalDateTime grantedAt,

        @Future(message = "Expiration date must be in the future")
        LocalDateTime expiresAt,

        @NotNull(message = "Active status is required")
        Boolean isActive
) {

    public UserStorageAccessDTO {

        if (accessLevel == null) {
            accessLevel = AccessLevel.BASIC;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    public UserStorageAccessDTO(UserStorageAccess access) {
        this(
                access != null ? access.getId() : null,
                access != null && access.getUser() != null ? access.getUser().getId() : null,
                access != null && access.getStorage() != null ? access.getStorage().getId() : null,
                access != null ? access.getAccessLevel() : AccessLevel.BASIC,
                access != null && access.getGrantedBy() != null ? access.getGrantedBy().getId() : null,
                access != null ? access.getGrantedAt() : null,
                access != null ? access.getExpiresAt() : null,
                access != null ? access.getIsActive() : true
        );
    }
}