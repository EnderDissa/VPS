package com.example.warehouse.mapper;

import com.example.warehouse.entity.ItemMaintenance;
import org.mapstruct.Mapper;
import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;

@Mapper(componentModel = "spring")
public interface ItemMaintenanceMapper {
    
    ItemMaintenanceDTO toDTO(ItemMaintenance object);

    ItemMaintenance toEntity(ItemMaintenanceDTO dto);
}
