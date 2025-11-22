package com.example.warehouse.mapper;
import org.mapstruct.Mapper;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface BorrowingMapper {

   @Mapping(target = "userId", source = "user.id")
   @Mapping(target = "itemId", source = "item.id")
   BorrowingDTO toDTO(Borrowing borrowing);

   @Mapping(target = "user.id", source = "userId")
   @Mapping(target = "item.id", source = "itemId")
   Borrowing toEntity(BorrowingDTO dto);
}