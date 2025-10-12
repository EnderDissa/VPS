package com.example.warehouse.mapper;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T17:14:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class KeepingMapperImpl implements KeepingMapper {

    @Override
    public KeepingDTO toDTO(Keeping object) {
        if ( object == null ) {
            return null;
        }

        Keeping keeping = null;

        KeepingDTO keepingDTO = new KeepingDTO( keeping );

        keepingDTO.setId( object.getId() );
        keepingDTO.setQuantity( object.getQuantity() );
        keepingDTO.setShelf( object.getShelf() );
        keepingDTO.setLastUpdated( object.getLastUpdated() );

        return keepingDTO;
    }

    @Override
    public Keeping toEntity(KeepingDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Keeping keeping = new Keeping();

        keeping.setId( dto.getId() );
        keeping.setQuantity( dto.getQuantity() );
        keeping.setShelf( dto.getShelf() );
        keeping.setLastUpdated( dto.getLastUpdated() );

        return keeping;
    }
}
