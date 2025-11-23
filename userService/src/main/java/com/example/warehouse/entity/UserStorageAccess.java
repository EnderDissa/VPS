package com.example.warehouse.entity;

import com.example.warehouse.enumeration.AccessLevel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_storage_access")
public class UserStorageAccess {

    @Id
    private Long id;


    @NotNull(message = "User ID is required")
    @Column("user_id")
    private Long userId;

    @NotNull(message = "Storage ID is required")
    @Column("storage_id")
    private Long storageId;

    @NotNull(message = "Access level is required")
    @Column("access_level")
    @Builder.Default
    private AccessLevel accessLevel = AccessLevel.BASIC;

    @NotNull(message = "Granted by user ID is required")
    @Column("granted_by")
    private Long grantedById;

    @Column("granted_at")
    private LocalDateTime grantedAt;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    @NotNull(message = "Active status is required")
    @Column("is_active")
    @Builder.Default
    private Boolean isActive = true;
}