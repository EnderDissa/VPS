package com.example.warehouse.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.dto.ItemMaintenanceDTO;

@Mapper(componentModel = "spring")
public interface ItemMaintenance {
    
    ItemMaintenanceDTO toDTO(ItemMaintenance object);

    ItemMaintenance toEntity(ItemMaintenanceDTO dto);
}
