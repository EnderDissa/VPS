package com.example.warehouse.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.infrastructure.web.dto.StorageDTO;
import com.example.warehouse.infrastructure.persistence.entity.Storage;

@Mapper(componentModel = "spring")
public interface StorageMapper {
    StorageDTO toDTO(Storage object);
    Storage toEntity(StorageDTO dto);
}
