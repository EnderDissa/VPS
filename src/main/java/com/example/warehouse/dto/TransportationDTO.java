package com.example.warehouse.dto;

import com.example.warehouse.entity.Transportation;
import com.example.warehouse.enumeration.TransportStatus;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransportationDTO {

    public TransportationDTO(Transportation transportation) {
        if (transportation == null) return;
        this.id = transportation.getId();
        this.itemId = transportation.getItem() != null ? transportation.getItem().getId() : null;
        this.vehicleId = transportation.getVehicle() != null ? transportation.getVehicle().getId() : null;
        this.driverId = transportation.getDriver() != null ? transportation.getDriver().getId() : null;
        this.fromStorageId = transportation.getFromStorage() != null ? transportation.getFromStorage().getId() : null;
        this.toStorageId = transportation.getToStorage() != null ? transportation.getToStorage().getId() : null;
        this.status = transportation.getStatus();
        this.scheduledDeparture = transportation.getScheduledDeparture();
        this.actualDeparture = transportation.getActualDeparture();
        this.scheduledArrival = transportation.getScheduledArrival();
        this.actualArrival = transportation.getActualArrival();
        this.createdAt = transportation.getCreatedAt();
    }

    private Long id;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotNull(message = "From storage ID is required")
    private Long fromStorageId;

    @NotNull(message = "To storage ID is required")
    private Long toStorageId;

    @NotNull(message = "Status is required")
    private TransportStatus status = TransportStatus.PLANNED;

    private LocalDateTime scheduledDeparture;
    private LocalDateTime actualDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualArrival;

    private LocalDateTime createdAt;
}