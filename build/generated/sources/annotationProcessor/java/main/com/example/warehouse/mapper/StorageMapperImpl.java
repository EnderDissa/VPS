package com.example.warehouse.mapper;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.entity.Storage;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T17:14:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class StorageMapperImpl implements StorageMapper {

    @Override
    public StorageDTO toDTO(Storage object) {
        if ( object == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String address = null;
        Integer capacity = null;
        LocalDateTime createdAt = null;

        id = object.getId();
        name = object.getName();
        address = object.getAddress();
        capacity = object.getCapacity();
        createdAt = object.getCreatedAt();

        StorageDTO storageDTO = new StorageDTO( id, name, address, capacity, createdAt );

        return storageDTO;
    }

    @Override
    public Storage toEntity(StorageDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Storage storage = new Storage();

        storage.setId( dto.getId() );
        storage.setName( dto.getName() );
        storage.setAddress( dto.getAddress() );
        storage.setCapacity( dto.getCapacity() );
        storage.setCreatedAt( dto.getCreatedAt() );

        return storage;
    }
}
