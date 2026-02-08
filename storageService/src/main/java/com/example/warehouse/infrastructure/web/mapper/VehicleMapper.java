package com.example.warehouse.infrastructure.web.mapper;

import com.example.warehouse.infrastructure.web.dto.VehicleDTO;
import com.example.warehouse.infrastructure.persistence.entity.Vehicle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    VehicleDTO toDTO(Vehicle object);
    Vehicle toEntity(VehicleDTO dto);
}
