package com.example.warehouse.infrastructure.web.mapper;

import com.example.warehouse.domain.model.File;
import com.example.warehouse.infrastructure.web.dto.FileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(target = "fileContent", ignore = true)
    FileDTO toDTO(File file);
    
    @Mapping(target = "fileContent", ignore = true)
    File toEntity(FileDTO filesDto);

    default List<FileDTO> toDTOList(List<File> files) {
        return files.stream().map(this::toDTO).toList();
    }
}
