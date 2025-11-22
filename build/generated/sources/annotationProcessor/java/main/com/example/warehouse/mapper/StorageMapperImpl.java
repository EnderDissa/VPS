package com.example.warehouse.mapper;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.entity.Storage;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
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

        Storage.StorageBuilder storage = Storage.builder();

        storage.id( dto.id() );
        storage.name( dto.name() );
        storage.address( dto.address() );
        storage.capacity( dto.capacity() );
        storage.createdAt( dto.createdAt() );

        return storage.build();
    }
}
