package com.example.warehouse.infrastructure.web.mapper;

import org.mapstruct.Mapper;

import com.example.warehouse.infrastructure.web.dto.KeepingDTO;
import com.example.warehouse.infrastructure.persistence.entity.Keeping;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KeepingMapper {

    @Mapping(target = "itemId", source = "item.id")
    KeepingDTO toDTO(Keeping object);

    @Mapping(target = "item.id", source = "itemId")
    Keeping toEntity(KeepingDTO dto);
}
