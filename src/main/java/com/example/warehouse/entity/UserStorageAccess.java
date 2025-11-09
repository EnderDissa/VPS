package com.example.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.example.warehouse.enumeration.AccessLevel;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_storage_access", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "storage_id"}))
public class UserStorageAccess {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Storage is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id", nullable = false)
    private Storage storage;

    @NotNull(message = "Access level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    private AccessLevel accessLevel = AccessLevel.BASIC;

    @NotNull(message = "Granted by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private User grantedBy;

    @CreationTimestamp
    @Column(name = "granted_at", updatable = false)
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
