package com.example.warehouse.dto;

import com.example.warehouse.entity.*;
import com.example.warehouse.enumeration.*;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

public class DtoConversionTest {

    @Test
    void shouldMapItemEntityToItemDTO() {
        Item item = new Item();
        item.setId(5L);
        item.setName("Laptop");
        item.setType(ItemType.ELECTRONICS);
        item.setCondition(ItemCondition.NEW);
        item.setSerialNumber("SN12345");
        item.setDescription("High-end laptop");
        item.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));

        ItemDTO dto = new ItemDTO(item);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.name()).isEqualTo("Laptop");
        assertThat(dto.type()).isEqualTo(ItemType.ELECTRONICS);
        assertThat(dto.condition()).isEqualTo(ItemCondition.NEW);
        assertThat(dto.serialNumber()).isEqualTo("SN12345");
        assertThat(dto.description()).isEqualTo("High-end laptop");
        assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    void shouldHandleNullItemInItemDTO() {
        ItemDTO dto = new ItemDTO(null);
        assertThat(dto.id()).isNull();
        assertThat(dto.name()).isNull();
        assertThat(dto.type()).isNull();
        assertThat(dto.condition()).isNull();
        assertThat(dto.serialNumber()).isNull();
        assertThat(dto.description()).isNull();
        assertThat(dto.createdAt()).isNull();
    }

    @Test
    void shouldMapTransportationEntityToDTO() {
        Transportation t = new Transportation();
        t.setId(500L);
        t.setItemId(501L);
        Vehicle vehicle = new Vehicle();
        vehicle.setId(502L);
        t.setVehicle(vehicle);
        t.setDriverId(503L);
        Storage from = new Storage();
        from.setId(504L);
        t.setFromStorage(from);
        Storage to = new Storage();
        to.setId(505L);
        t.setToStorage(to);
        t.setStatus(TransportStatus.IN_TRANSIT);
        t.setScheduledDeparture(LocalDateTime.of(2025, 11, 1, 8, 0));
        t.setActualDeparture(LocalDateTime.of(2025, 11, 1, 8, 15));
        t.setScheduledArrival(LocalDateTime.of(2025, 11, 1, 12, 0));
        t.setActualArrival(LocalDateTime.of(2025, 11, 1, 11, 45));
        t.setCreatedAt(LocalDateTime.of(2025, 10, 27, 10, 0));

        TransportationDTO dto = new TransportationDTO(t);

        assertThat(dto.id()).isEqualTo(500L);
        assertThat(dto.itemId()).isEqualTo(501L);
        assertThat(dto.vehicleId()).isEqualTo(502L);
        assertThat(dto.driverId()).isEqualTo(503L);
        assertThat(dto.fromStorageId()).isEqualTo(504L);
        assertThat(dto.toStorageId()).isEqualTo(505L);
        assertThat(dto.status()).isEqualTo(TransportStatus.IN_TRANSIT);
        assertThat(dto.scheduledDeparture()).isEqualTo(LocalDateTime.of(2025, 11, 1, 8, 0));
        assertThat(dto.actualDeparture()).isEqualTo(LocalDateTime.of(2025, 11, 1, 8, 15));
        assertThat(dto.scheduledArrival()).isEqualTo(LocalDateTime.of(2025, 11, 1, 12, 0));
        assertThat(dto.actualArrival()).isEqualTo(LocalDateTime.of(2025, 11, 1, 11, 45));
        assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2025, 10, 27, 10, 0));
    }

    @Test
    void shouldUseDefaultStatusWhenTransportationStatusIsNull() {
        Transportation t = new Transportation();
        t.setStatus(null);
        TransportationDTO dto = new TransportationDTO(t);
        assertThat(dto.status()).isEqualTo(TransportStatus.PLANNED);
    }

    @Test
    void shouldHandleNullTransportation() {
        TransportationDTO dto = new TransportationDTO(null);
        assertThat(dto.id()).isNull();
        assertThat(dto.itemId()).isNull();
        assertThat(dto.vehicleId()).isNull();
        assertThat(dto.driverId()).isNull();
        assertThat(dto.fromStorageId()).isNull();
        assertThat(dto.toStorageId()).isNull();
        assertThat(dto.status()).isEqualTo(TransportStatus.PLANNED);
        assertThat(dto.scheduledDeparture()).isNull();
        assertThat(dto.actualDeparture()).isNull();
        assertThat(dto.scheduledArrival()).isNull();
        assertThat(dto.actualArrival()).isNull();
        assertThat(dto.createdAt()).isNull();
    }

    @Test
    void shouldMapStorageEntityToStorageDTO() {
        Storage storage = new Storage();
        storage.setId(1000L);
        storage.setName("Main Warehouse");
        storage.setAddress("123 Logistics Blvd, City");
        storage.setCapacity(5000);
        LocalDateTime created = LocalDateTime.of(2024, 3, 15, 9, 0);
        storage.setCreatedAt(created);

        StorageDTO dto = new StorageDTO(storage);

        assertThat(dto.id()).isEqualTo(1000L);
        assertThat(dto.name()).isEqualTo("Main Warehouse");
        assertThat(dto.address()).isEqualTo("123 Logistics Blvd, City");
        assertThat(dto.capacity()).isEqualTo(5000);
        assertThat(dto.createdAt()).isEqualTo(created);
    }

    @Test
    void shouldMapVehicleEntityToDTO() {
        Vehicle v = new Vehicle();
        v.setId(700L);
        v.setBrand("Toyota");
        v.setModel("Camry");
        v.setLicensePlate("ABC123");
        v.setYear(2023);
        v.setCapacity(5);
        v.setStatus(VehicleStatus.IN_USE);

        VehicleDTO dto = new VehicleDTO(v);

        assertThat(dto.id()).isEqualTo(700L);
        assertThat(dto.brand()).isEqualTo("Toyota");
        assertThat(dto.model()).isEqualTo("Camry");
        assertThat(dto.licensePlate()).isEqualTo("ABC123");
        assertThat(dto.year()).isEqualTo(2023);
        assertThat(dto.capacity()).isEqualTo(5);
        assertThat(dto.status()).isEqualTo(VehicleStatus.IN_USE);
    }

    @Test
    void shouldUseDefaultStatusWhenVehicleStatusIsNull() {
        Vehicle v = new Vehicle();
        v.setStatus(null);
        VehicleDTO dto = new VehicleDTO(v);
        assertThat(dto.status()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void shouldHandleNullVehicle() {
        VehicleDTO dto = new VehicleDTO(null);
        assertThat(dto.id()).isNull();
        assertThat(dto.brand()).isNull();
        assertThat(dto.model()).isNull();
        assertThat(dto.licensePlate()).isNull();
        assertThat(dto.year()).isNull();
        assertThat(dto.capacity()).isNull();
        assertThat(dto.status()).isEqualTo(VehicleStatus.AVAILABLE);
    }
}