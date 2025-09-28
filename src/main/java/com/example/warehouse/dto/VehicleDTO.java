package com.example.warehouse.dto;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleDTO {

    public VehicleDTO(Vehicle vehicle) {
        if (vehicle == null) return;
        this.id = vehicle.getId();
        this.brand = vehicle.getBrand();
        this.model = vehicle.getModel();
        this.licensePlate = vehicle.getLicensePlate();
        this.year = vehicle.getYear();
        this.capacity = vehicle.getCapacity();
        this.status = vehicle.getStatus();
    }

    private Long id;

    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @PositiveOrZero(message = "Year must be positive")
    private Integer year;

    @PositiveOrZero(message = "Capacity must be positive or zero")
    private Integer capacity;

    @NotNull(message = "Status is required")
    private VehicleStatus status = VehicleStatus.AVAILABLE;
}