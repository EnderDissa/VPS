package com.example.warehouse.domain.model;

import com.example.warehouse.domain.enumeration.AccessLevel;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStorageAccess {

    private Long id;
    private Long userId;
    private Long storageId;
    private AccessLevel accessLevel = AccessLevel.BASIC;
    private Long grantedById;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive = true;
}