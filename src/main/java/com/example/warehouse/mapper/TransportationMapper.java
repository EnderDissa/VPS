package com.example.warehouse.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;

@Mapper(componentModel = "spring")
public interface TransportationMapper {
    TransportationDTO toDTO(Transportation object);
    Transportation toEntity(TransportationDTO dto);
}
