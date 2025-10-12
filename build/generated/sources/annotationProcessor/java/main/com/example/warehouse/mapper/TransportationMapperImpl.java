package com.example.warehouse.mapper;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T17:14:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class TransportationMapperImpl implements TransportationMapper {

    @Override
    public TransportationDTO toDTO(Transportation object) {
        if ( object == null ) {
            return null;
        }

        Transportation transportation = null;

        TransportationDTO transportationDTO = new TransportationDTO( transportation );

        transportationDTO.setId( object.getId() );
        transportationDTO.setStatus( object.getStatus() );
        transportationDTO.setScheduledDeparture( object.getScheduledDeparture() );
        transportationDTO.setActualDeparture( object.getActualDeparture() );
        transportationDTO.setScheduledArrival( object.getScheduledArrival() );
        transportationDTO.setActualArrival( object.getActualArrival() );
        transportationDTO.setCreatedAt( object.getCreatedAt() );

        return transportationDTO;
    }

    @Override
    public Transportation toEntity(TransportationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Transportation transportation = new Transportation();

        transportation.setId( dto.getId() );
        transportation.setStatus( dto.getStatus() );
        transportation.setScheduledDeparture( dto.getScheduledDeparture() );
        transportation.setActualDeparture( dto.getActualDeparture() );
        transportation.setScheduledArrival( dto.getScheduledArrival() );
        transportation.setActualArrival( dto.getActualArrival() );
        transportation.setCreatedAt( dto.getCreatedAt() );

        return transportation;
    }
}
