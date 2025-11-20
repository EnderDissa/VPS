package com.example.warehouse.mapper;

import org.mapstruct.Mapper;
import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;


@Mapper(componentModel = "spring")
public interface ItemMapper {
   ItemDTO toDTO(Item object);

   Item toEntity(ItemDTO dto);
}