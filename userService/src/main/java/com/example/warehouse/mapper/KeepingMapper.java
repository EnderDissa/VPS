package com.example.warehouse.mapper;

import org.mapstruct.Mapper;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KeepingMapper {

    @Mapping(target = "storageId", source = "storage.id")
    @Mapping(target = "itemId", source = "item.id")
    KeepingDTO toDTO(Keeping object);

    @Mapping(target = "storage.id", source = "storageId")
    @Mapping(target = "item.id", source = "itemId")
    Keeping toEntity(KeepingDTO dto);
}
