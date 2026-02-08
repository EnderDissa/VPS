package com.example.warehouse.mapper;

import com.example.warehouse.domain.enumeration.TransportStatus;
import com.example.warehouse.domain.enumeration.VehicleStatus;
import com.example.warehouse.infrastructure.persistence.entity.Storage;
import com.example.warehouse.infrastructure.persistence.entity.Transportation;
import com.example.warehouse.infrastructure.persistence.entity.Vehicle;
import com.example.warehouse.infrastructure.web.dto.StorageDTO;
import com.example.warehouse.infrastructure.web.dto.TransportationDTO;
import com.example.warehouse.infrastructure.web.dto.VehicleDTO;
import com.example.warehouse.infrastructure.web.mapper.StorageMapper;
import com.example.warehouse.infrastructure.web.mapper.TransportationMapper;
import com.example.warehouse.infrastructure.web.mapper.VehicleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@WebFluxTest (
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        TransportationMapper.class,
                        VehicleMapper.class,
                        StorageMapper.class,
                }),
        }
)
public class MapperToEntityTest {


    @Autowired
    private TransportationMapper transportationMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private StorageMapper storageMapper;

    @Test
    void shouldMapTransportationDTOToEntity() {
        TransportationDTO dto = new TransportationDTO(
                500L,
                501L,
                502L,
                503L,
                504L,
                505L,
                TransportStatus.IN_TRANSIT,
                LocalDateTime.of(2025, 11, 1, 8, 0),
                LocalDateTime.of(2025, 11, 1, 8, 15),
                LocalDateTime.of(2025, 11, 1, 12, 0),
                LocalDateTime.of(2025, 11, 1, 11, 45),
                LocalDateTime.of(2025, 10, 27, 10, 0)
        );

        Transportation entity = transportationMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(500L);
        assertThat(entity.getStatus()).isEqualTo(TransportStatus.IN_TRANSIT);
        assertThat(entity.getScheduledDeparture()).isEqualTo(LocalDateTime.of(2025, 11, 1, 8, 0));
        assertThat(entity.getActualDeparture()).isEqualTo(LocalDateTime.of(2025, 11, 1, 8, 15));
        assertThat(entity.getScheduledArrival()).isEqualTo(LocalDateTime.of(2025, 11, 1, 12, 0));
        assertThat(entity.getActualArrival()).isEqualTo(LocalDateTime.of(2025, 11, 1, 11, 45));
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 10, 27, 10, 0));

        assertThat(entity.getItemId()).isNotNull();
        assertThat(entity.getVehicle()).isNotNull();
        assertThat(entity.getDriverId()).isNotNull();
        assertThat(entity.getFromStorage()).isNotNull();
        assertThat(entity.getToStorage()).isNotNull();
    }

    @Test
    void shouldReturnNullWhenTransportationDTOIsNull() {
        assertThat(transportationMapper.toEntity(null)).isNull();
    }

    @Test
    void shouldMapVehicleDTOToEntity() {
        VehicleDTO dto = new VehicleDTO(
                700L,
                "Toyota",
                "Camry",
                "ABC123",
                2023,
                5,
                VehicleStatus.IN_USE
        );

        Vehicle entity = vehicleMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(700L);
        assertThat(entity.getBrand()).isEqualTo("Toyota");
        assertThat(entity.getModel()).isEqualTo("Camry");
        assertThat(entity.getLicensePlate()).isEqualTo("ABC123");
        assertThat(entity.getYear()).isEqualTo(2023);
        assertThat(entity.getCapacity()).isEqualTo(5);
        assertThat(entity.getStatus()).isEqualTo(VehicleStatus.IN_USE);
    }

    @Test
    void shouldReturnNullWhenVehicleDTOIsNull() {
        assertThat(vehicleMapper.toEntity(null)).isNull();
    }

    @Test
    void shouldMapStorageDTOToEntity() {
        StorageDTO dto = new StorageDTO(
                1000L,
                "Main Warehouse",
                "123 Logistics Blvd, City",
                5000,
                LocalDateTime.of(2024, 3, 15, 9, 0)
        );

        Storage entity = storageMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1000L);
        assertThat(entity.getName()).isEqualTo("Main Warehouse");
        assertThat(entity.getAddress()).isEqualTo("123 Logistics Blvd, City");
        assertThat(entity.getCapacity()).isEqualTo(5000);
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 3, 15, 9, 0));
    }

    @Test
    void shouldReturnNullWhenStorageDTOIsNull() {
        assertThat(storageMapper.toEntity(null)).isNull();
    }
}