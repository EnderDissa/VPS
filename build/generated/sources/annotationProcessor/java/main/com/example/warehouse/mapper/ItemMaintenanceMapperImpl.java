package com.example.warehouse.mapper;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T17:14:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class ItemMaintenanceMapperImpl implements ItemMaintenanceMapper {

    @Override
    public ItemMaintenanceDTO toDTO(ItemMaintenance object) {
        if ( object == null ) {
            return null;
        }

        ItemMaintenance maintenance = null;

        ItemMaintenanceDTO itemMaintenanceDTO = new ItemMaintenanceDTO( maintenance );

        itemMaintenanceDTO.setId( object.getId() );
        itemMaintenanceDTO.setMaintenanceDate( object.getMaintenanceDate() );
        itemMaintenanceDTO.setNextMaintenanceDate( object.getNextMaintenanceDate() );
        itemMaintenanceDTO.setCost( object.getCost() );
        itemMaintenanceDTO.setDescription( object.getDescription() );
        itemMaintenanceDTO.setStatus( object.getStatus() );
        itemMaintenanceDTO.setCreatedAt( object.getCreatedAt() );

        return itemMaintenanceDTO;
    }

    @Override
    public ItemMaintenance toEntity(ItemMaintenanceDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ItemMaintenance itemMaintenance = new ItemMaintenance();

        itemMaintenance.setId( dto.getId() );
        itemMaintenance.setMaintenanceDate( dto.getMaintenanceDate() );
        itemMaintenance.setNextMaintenanceDate( dto.getNextMaintenanceDate() );
        itemMaintenance.setCost( dto.getCost() );
        itemMaintenance.setDescription( dto.getDescription() );
        itemMaintenance.setStatus( dto.getStatus() );
        itemMaintenance.setCreatedAt( dto.getCreatedAt() );

        return itemMaintenance;
    }
}
