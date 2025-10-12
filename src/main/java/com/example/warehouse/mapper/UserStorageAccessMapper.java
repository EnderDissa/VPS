package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStorageAccessMapper {
    UserStorageAccessDTO toDTO(UserStorageAccess object);
    UserStorageAccess toEntity(UserStorageAccessDTO dto);
}
