package com.example.warehouse.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransportationMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "fromStorageId", source = "fromStorage.id")
    @Mapping(target = "toStorageId", source = "toStorage.id")
    TransportationDTO toDTO(Transportation object);

    @Mapping(target = "item.id", source = "itemId")
    @Mapping(target = "vehicle.id", source = "vehicleId")
    @Mapping(target = "driver.id", source = "driverId")
    @Mapping(target = "fromStorage.id", source = "fromStorageId")
    @Mapping(target = "toStorage.id", source = "toStorageId")
    Transportation toEntity(TransportationDTO dto);
}
