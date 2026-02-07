package com.example.warehouse.infrastructure.web.mapper;

import com.example.warehouse.infrastructure.persistence.entity.ItemMaintenance;
import org.mapstruct.Mapper;
import com.example.warehouse.infrastructure.web.dto.ItemMaintenanceDTO;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMaintenanceMapper {

    @Mapping(target = "itemId", source = "item.id")
    ItemMaintenanceDTO toDTO(ItemMaintenance object);

    @Mapping(target = "item.id", source = "itemId")
    ItemMaintenance toEntity(ItemMaintenanceDTO dto);
}
