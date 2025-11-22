package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserStorageAccessMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "storageId", source = "storage.id")
    @Mapping(target = "grantedById", source = "grantedBy.id")
    UserStorageAccessDTO toDTO(UserStorageAccess object);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "storage.id", source = "storageId")
    @Mapping(target = "grantedBy.id", source = "grantedById")
    UserStorageAccess toEntity(UserStorageAccessDTO dto);
}
