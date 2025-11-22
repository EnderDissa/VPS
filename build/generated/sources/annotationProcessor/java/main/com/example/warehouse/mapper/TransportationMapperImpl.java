package com.example.warehouse.mapper;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.entity.Transportation;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.TransportStatus;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class TransportationMapperImpl implements TransportationMapper {

    @Override
    public TransportationDTO toDTO(Transportation object) {
        if ( object == null ) {
            return null;
        }

        Long itemId = null;
        Long vehicleId = null;
        Long driverId = null;
        Long fromStorageId = null;
        Long toStorageId = null;
        Long id = null;
        TransportStatus status = null;
        LocalDateTime scheduledDeparture = null;
        LocalDateTime actualDeparture = null;
        LocalDateTime scheduledArrival = null;
        LocalDateTime actualArrival = null;
        LocalDateTime createdAt = null;

        itemId = objectItemId( object );
        vehicleId = objectVehicleId( object );
        driverId = objectDriverId( object );
        fromStorageId = objectFromStorageId( object );
        toStorageId = objectToStorageId( object );
        id = object.getId();
        status = object.getStatus();
        scheduledDeparture = object.getScheduledDeparture();
        actualDeparture = object.getActualDeparture();
        scheduledArrival = object.getScheduledArrival();
        actualArrival = object.getActualArrival();
        createdAt = object.getCreatedAt();

        TransportationDTO transportationDTO = new TransportationDTO( id, itemId, vehicleId, driverId, fromStorageId, toStorageId, status, scheduledDeparture, actualDeparture, scheduledArrival, actualArrival, createdAt );

        return transportationDTO;
    }

    @Override
    public Transportation toEntity(TransportationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Transportation.TransportationBuilder transportation = Transportation.builder();

        transportation.item( transportationDTOToItem( dto ) );
        transportation.vehicle( transportationDTOToVehicle( dto ) );
        transportation.driver( transportationDTOToUser( dto ) );
        transportation.fromStorage( transportationDTOToStorage( dto ) );
        transportation.toStorage( transportationDTOToStorage1( dto ) );
        transportation.id( dto.id() );
        transportation.status( dto.status() );
        transportation.scheduledDeparture( dto.scheduledDeparture() );
        transportation.actualDeparture( dto.actualDeparture() );
        transportation.scheduledArrival( dto.scheduledArrival() );
        transportation.actualArrival( dto.actualArrival() );
        transportation.createdAt( dto.createdAt() );

        return transportation.build();
    }

    private Long objectItemId(Transportation transportation) {
        Item item = transportation.getItem();
        if ( item == null ) {
            return null;
        }
        return item.getId();
    }

    private Long objectVehicleId(Transportation transportation) {
        Vehicle vehicle = transportation.getVehicle();
        if ( vehicle == null ) {
            return null;
        }
        return vehicle.getId();
    }

    private Long objectDriverId(Transportation transportation) {
        User driver = transportation.getDriver();
        if ( driver == null ) {
            return null;
        }
        return driver.getId();
    }

    private Long objectFromStorageId(Transportation transportation) {
        Storage fromStorage = transportation.getFromStorage();
        if ( fromStorage == null ) {
            return null;
        }
        return fromStorage.getId();
    }

    private Long objectToStorageId(Transportation transportation) {
        Storage toStorage = transportation.getToStorage();
        if ( toStorage == null ) {
            return null;
        }
        return toStorage.getId();
    }

    protected Item transportationDTOToItem(TransportationDTO transportationDTO) {
        if ( transportationDTO == null ) {
            return null;
        }

        Item.ItemBuilder item = Item.builder();

        item.id( transportationDTO.itemId() );

        return item.build();
    }

    protected Vehicle transportationDTOToVehicle(TransportationDTO transportationDTO) {
        if ( transportationDTO == null ) {
            return null;
        }

        Vehicle.VehicleBuilder vehicle = Vehicle.builder();

        vehicle.id( transportationDTO.vehicleId() );

        return vehicle.build();
    }

    protected User transportationDTOToUser(TransportationDTO transportationDTO) {
        if ( transportationDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( transportationDTO.driverId() );

        return user.build();
    }

    protected Storage transportationDTOToStorage(TransportationDTO transportationDTO) {
        if ( transportationDTO == null ) {
            return null;
        }

        Storage.StorageBuilder storage = Storage.builder();

        storage.id( transportationDTO.fromStorageId() );

        return storage.build();
    }

    protected Storage transportationDTOToStorage1(TransportationDTO transportationDTO) {
        if ( transportationDTO == null ) {
            return null;
        }

        Storage.StorageBuilder storage = Storage.builder();

        storage.id( transportationDTO.toStorageId() );

        return storage.build();
    }
}
