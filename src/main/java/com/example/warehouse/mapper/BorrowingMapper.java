package com.example.warehouse.mapper;
import org.mapstruct.Mapper;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;


@Mapper(componentModel = "spring")
public interface BorrowingMapper {
   BorrowingDTO toDTO(Borrowing object);

   Borrowing toEntity(BorrowingDTO dto);
}