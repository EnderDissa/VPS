package com.example.warehouse.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.infrastructure.web.dto.TransportationDTO;
import com.example.warehouse.infrastructure.persistence.entity.Transportation;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransportationMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "fromStorageId", source = "fromStorage.id")
    @Mapping(target = "toStorageId", source = "toStorage.id")
    TransportationDTO toDTO(Transportation object);

    @Mapping(target = "vehicle.id", source = "vehicleId")
    @Mapping(target = "fromStorage.id", source = "fromStorageId")
    @Mapping(target = "toStorage.id", source = "toStorageId")
    Transportation toEntity(TransportationDTO dto);
}
