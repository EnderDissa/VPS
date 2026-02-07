package com.example.warehouse.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.infrastructure.web.dto.ItemDTO;
import com.example.warehouse.infrastructure.persistence.entity.Item;


@Mapper(componentModel = "spring")
public interface ItemMapper {
   ItemDTO toDTO(Item object);

   Item toEntity(ItemDTO dto);
}