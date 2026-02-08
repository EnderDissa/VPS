package com.example.warehouse.domain.model;

import com.example.warehouse.domain.enumeration.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    private Long id;
    private String brand;
    private String model;
    private String licensePlate;
    private Integer year;
    private Integer capacity;
    private VehicleStatus status = VehicleStatus.AVAILABLE;
}
