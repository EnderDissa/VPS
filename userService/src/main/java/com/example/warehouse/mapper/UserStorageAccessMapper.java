package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserStorageAccessMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "storageId", source = "storageId")
    @Mapping(target = "grantedById", source = "grantedById")
    UserStorageAccessDTO toDTO(UserStorageAccess object);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "storageId", source = "storageId")
    @Mapping(target = "grantedById", source = "grantedById")
    UserStorageAccess toEntity(UserStorageAccessDTO dto);
}