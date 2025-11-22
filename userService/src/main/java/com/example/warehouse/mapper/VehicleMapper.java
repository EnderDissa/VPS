package com.example.warehouse.mapper;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.entity.Vehicle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    VehicleDTO toDTO(Vehicle object);
    Vehicle toEntity(VehicleDTO dto);
}
