package com.example.warehouse.dto;

import com.example.warehouse.entity.Transportation;
import com.example.warehouse.enumeration.TransportStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TransportationDTO(
        Long id,

        @NotNull(message = "Item ID is required")
        Long itemId,

        @NotNull(message = "Vehicle ID is required")
        Long vehicleId,

        @NotNull(message = "Driver ID is required")
        Long driverId,

        @NotNull(message = "From storage ID is required")
        Long fromStorageId,

        @NotNull(message = "To storage ID is required")
        Long toStorageId,

        @NotNull(message = "Status is required")
        TransportStatus status,

        LocalDateTime scheduledDeparture,
        LocalDateTime actualDeparture,
        LocalDateTime scheduledArrival,
        LocalDateTime actualArrival,

        LocalDateTime createdAt
) {

    public TransportationDTO {
        if (status == null) {
            status = TransportStatus.PLANNED;
        }
    }

    public TransportationDTO(Transportation transportation) {
        this(
                transportation != null ? transportation.getId() : null,
                transportation != null && transportation.getItem() != null ? transportation.getItem().getId() : null,
                transportation != null && transportation.getVehicle() != null ? transportation.getVehicle().getId() : null,
                transportation != null && transportation.getDriver() != null ? transportation.getDriver().getId() : null,
                transportation != null && transportation.getFromStorage() != null ? transportation.getFromStorage().getId() : null,
                transportation != null && transportation.getToStorage() != null ? transportation.getToStorage().getId() : null,
                transportation != null ? transportation.getStatus() : TransportStatus.PLANNED,
                transportation != null ? transportation.getScheduledDeparture() : null,
                transportation != null ? transportation.getActualDeparture() : null,
                transportation != null ? transportation.getScheduledArrival() : null,
                transportation != null ? transportation.getActualArrival() : null,
                transportation != null ? transportation.getCreatedAt() : null
        );
    }
}