package com.example.warehouse.mapper;

import com.example.warehouse.entity.ItemMaintenance;
import org.mapstruct.Mapper;
import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMaintenanceMapper {

    @Mapping(target = "itemId", source = "item.id")
    ItemMaintenanceDTO toDTO(ItemMaintenance object);

    @Mapping(target = "item.id", source = "itemId")
    ItemMaintenance toEntity(ItemMaintenanceDTO dto);
}
