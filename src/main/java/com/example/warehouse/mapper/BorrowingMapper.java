package com.example.warehouse.mapper;
import org.mapstruct.Mapper;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface BorrowingMapper {
   @Mapping(target = "itemId", source = "item.id")
   @Mapping(target = "userId", source = "user.id")
   BorrowingDTO toDTO(Borrowing object);

   @Mapping(target = "item.id", source = "itemId")
   @Mapping(target = "user.id", source = "userId")
   Borrowing toEntity(BorrowingDTO dto);
}