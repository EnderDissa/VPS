package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;

public interface UserStorageAccessMapper {
    UserStorageAccessDTO toDTO(UserStorageAccess object);
    UserStorageAccess toEntity(UserStorageAccessDTO dto);
}
