package com.example.warehouse.mapper;

import com.example.warehouse.entity.ItemMaintenance;
import org.mapstruct.Mapper;
import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMaintenanceMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "technicianId", source = "technician.id")
    ItemMaintenanceDTO toDTO(ItemMaintenance object);

    @Mapping(target = "item.id", source = "itemId")
    @Mapping(target = "technician.id", source = "technicianId")
    ItemMaintenance toEntity(ItemMaintenanceDTO dto);
}
