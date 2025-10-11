package com.example.warehouse.mapper;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.entity.Vehicle;

public interface VehicleMapper {
    VehicleDTO toDTO(Vehicle object);
    Vehicle toEntity(VehicleDTO dto);
}
