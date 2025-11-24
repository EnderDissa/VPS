package com.example.warehouse.dto;

import com.example.warehouse.entity.Transportation;
import com.example.warehouse.enumeration.TransportStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TransportationDTO(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
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

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime scheduledDeparture,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime actualDeparture,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime scheduledArrival,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime actualArrival,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
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