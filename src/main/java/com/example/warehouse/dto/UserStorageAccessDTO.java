package com.example.warehouse.dto;

import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserStorageAccessDTO {

    public UserStorageAccessDTO(UserStorageAccess access) {
        if (access == null) return;
        this.id = access.getId();
        this.userId = access.getUser() != null ? access.getUser().getId() : null;
        this.storageId = access.getStorage() != null ? access.getStorage().getId() : null;
        this.accessLevel = access.getAccessLevel();
        this.grantedById = access.getGrantedBy() != null ? access.getGrantedBy().getId() : null;
        this.grantedAt = access.getGrantedAt();
        this.expiresAt = access.getExpiresAt();
        this.isActive = access.getIsActive();
    }

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Storage ID is required")
    private Long storageId;

    @NotNull(message = "Access level is required")
    private AccessLevel accessLevel = AccessLevel.BASIC;

    @NotNull(message = "Granted by user ID is required")
    private Long grantedById;

    private LocalDateTime grantedAt;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
}
