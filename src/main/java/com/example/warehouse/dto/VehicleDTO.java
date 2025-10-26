package com.example.warehouse.dto;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehicleDTO(
        Long id,

        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        String brand,

        @NotBlank(message = "Model is required")
        @Size(max = 100, message = "Model must not exceed 100 characters")
        String model,

        @NotBlank(message = "License plate is required")
        @Size(max = 20, message = "License plate must not exceed 20 characters")
        String licensePlate,

        @PositiveOrZero(message = "Year must be positive")
        Integer year,

        @PositiveOrZero(message = "Capacity must be positive or zero")
        Integer capacity,

        @NotNull(message = "Status is required")
        VehicleStatus status
) {

    public VehicleDTO {
        if (status == null) {
            status = VehicleStatus.AVAILABLE;
        }
    }

    public VehicleDTO(Vehicle vehicle) {
        this(
                vehicle != null ? vehicle.getId() : null,
                vehicle != null ? vehicle.getBrand() : null,
                vehicle != null ? vehicle.getModel() : null,
                vehicle != null ? vehicle.getLicensePlate() : null,
                vehicle != null ? vehicle.getYear() : null,
                vehicle != null ? vehicle.getCapacity() : null,
                vehicle != null ? vehicle.getStatus() : VehicleStatus.AVAILABLE
        );
    }
}