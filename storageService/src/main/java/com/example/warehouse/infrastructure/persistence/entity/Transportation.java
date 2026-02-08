package com.example.warehouse.infrastructure.persistence.entity;

import com.example.warehouse.domain.enumeration.TransportStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transportations")
public class Transportation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Item is required")
    private Long itemId;

    @NotNull(message = "Vehicle is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull(message = "Driver is required")
    private Long driverId;

    @NotNull(message = "From storage is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_storage_id", nullable = false)
    private Storage fromStorage;

    @NotNull(message = "To storage is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_storage_id", nullable = false)
    private Storage toStorage;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportStatus status = TransportStatus.PLANNED;

    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;

    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
