package com.example.warehouse.mapper;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class VehicleMapperImpl implements VehicleMapper {

    @Override
    public VehicleDTO toDTO(Vehicle object) {
        if ( object == null ) {
            return null;
        }

        Long id = null;
        String brand = null;
        String model = null;
        String licensePlate = null;
        Integer year = null;
        Integer capacity = null;
        VehicleStatus status = null;

        id = object.getId();
        brand = object.getBrand();
        model = object.getModel();
        licensePlate = object.getLicensePlate();
        year = object.getYear();
        capacity = object.getCapacity();
        status = object.getStatus();

        VehicleDTO vehicleDTO = new VehicleDTO( id, brand, model, licensePlate, year, capacity, status );

        return vehicleDTO;
    }

    @Override
    public Vehicle toEntity(VehicleDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Vehicle.VehicleBuilder vehicle = Vehicle.builder();

        vehicle.id( dto.id() );
        vehicle.brand( dto.brand() );
        vehicle.model( dto.model() );
        vehicle.licensePlate( dto.licensePlate() );
        vehicle.year( dto.year() );
        vehicle.capacity( dto.capacity() );
        vehicle.status( dto.status() );

        return vehicle.build();
    }
}
