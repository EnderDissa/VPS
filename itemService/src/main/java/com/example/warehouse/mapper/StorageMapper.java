package com.example.warehouse.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.entity.Storage;

@Mapper(componentModel = "spring")
public interface StorageMapper {
    StorageDTO toDTO(Storage object);
    Storage toEntity(StorageDTO dto);
}
