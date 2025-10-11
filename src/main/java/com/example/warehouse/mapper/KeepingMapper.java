package com.example.warehouse.mapper;

import org.mapstruct.Mapper;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;

@Mapper(componentModel = "spring")
public interface KeepingMapper {
    KeepingDTO toDTO(Keeping object);
    Keeping toEntity(KeepingDTO dto);
}
