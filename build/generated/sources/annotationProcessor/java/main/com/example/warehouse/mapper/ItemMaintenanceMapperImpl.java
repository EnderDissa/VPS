package com.example.warehouse.mapper;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.MaintenanceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class ItemMaintenanceMapperImpl implements ItemMaintenanceMapper {

    @Override
    public ItemMaintenanceDTO toDTO(ItemMaintenance object) {
        if ( object == null ) {
            return null;
        }

        Long itemId = null;
        Long technicianId = null;
        Long id = null;
        LocalDateTime maintenanceDate = null;
        LocalDateTime nextMaintenanceDate = null;
        BigDecimal cost = null;
        String description = null;
        MaintenanceStatus status = null;
        LocalDateTime createdAt = null;

        itemId = objectItemId( object );
        technicianId = objectTechnicianId( object );
        id = object.getId();
        maintenanceDate = object.getMaintenanceDate();
        nextMaintenanceDate = object.getNextMaintenanceDate();
        cost = object.getCost();
        description = object.getDescription();
        status = object.getStatus();
        createdAt = object.getCreatedAt();

        ItemMaintenanceDTO itemMaintenanceDTO = new ItemMaintenanceDTO( id, itemId, technicianId, maintenanceDate, nextMaintenanceDate, cost, description, status, createdAt );

        return itemMaintenanceDTO;
    }

    @Override
    public ItemMaintenance toEntity(ItemMaintenanceDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ItemMaintenance.ItemMaintenanceBuilder itemMaintenance = ItemMaintenance.builder();

        itemMaintenance.item( itemMaintenanceDTOToItem( dto ) );
        itemMaintenance.technician( itemMaintenanceDTOToUser( dto ) );
        itemMaintenance.id( dto.id() );
        itemMaintenance.maintenanceDate( dto.maintenanceDate() );
        itemMaintenance.nextMaintenanceDate( dto.nextMaintenanceDate() );
        itemMaintenance.cost( dto.cost() );
        itemMaintenance.description( dto.description() );
        itemMaintenance.status( dto.status() );
        itemMaintenance.createdAt( dto.createdAt() );

        return itemMaintenance.build();
    }

    private Long objectItemId(ItemMaintenance itemMaintenance) {
        Item item = itemMaintenance.getItem();
        if ( item == null ) {
            return null;
        }
        return item.getId();
    }

    private Long objectTechnicianId(ItemMaintenance itemMaintenance) {
        User technician = itemMaintenance.getTechnician();
        if ( technician == null ) {
            return null;
        }
        return technician.getId();
    }

    protected Item itemMaintenanceDTOToItem(ItemMaintenanceDTO itemMaintenanceDTO) {
        if ( itemMaintenanceDTO == null ) {
            return null;
        }

        Item.ItemBuilder item = Item.builder();

        item.id( itemMaintenanceDTO.itemId() );

        return item.build();
    }

    protected User itemMaintenanceDTOToUser(ItemMaintenanceDTO itemMaintenanceDTO) {
        if ( itemMaintenanceDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( itemMaintenanceDTO.technicianId() );

        return user.build();
    }
}
