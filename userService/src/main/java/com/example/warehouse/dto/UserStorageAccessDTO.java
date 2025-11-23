package com.example.warehouse.dto;

import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserStorageAccessDTO(
        @Schema(hidden = true)
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

}
