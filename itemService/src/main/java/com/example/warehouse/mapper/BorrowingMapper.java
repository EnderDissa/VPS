package com.example.warehouse.mapper;
import org.mapstruct.Mapper;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface BorrowingMapper {

   @Mapping(target = "itemId", source = "item.id")
   BorrowingDTO toDTO(Borrowing borrowing);

   @Mapping(target = "item.id", source = "itemId")
   Borrowing toEntity(BorrowingDTO dto);
}