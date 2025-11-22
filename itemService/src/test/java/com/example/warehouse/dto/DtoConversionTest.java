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
    void shouldMapBorrowingEntityToBorrowingDTO() {
        Item item = new Item();
        item.setId(10L);
        User user = new User();
        user.setId(20L);
        Borrowing borrowing = Borrowing.builder()
            .id(1L)
            .item(item)
            .user(user)
            .quantity(3)
            .borrowDate(LocalDateTime.of(2025, 10, 1, 10, 0))
            .expectedReturnDate(LocalDateTime.of(2025, 11, 1, 10, 0))
            .actualReturnDate(LocalDateTime.of(2025, 10, 15, 14, 0))
            .status(BorrowStatus.RETURNED)
            .purpose("Project X")
            .build();
        BorrowingDTO dto = new BorrowingDTO(borrowing);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.itemId()).isEqualTo(10L);
        assertThat(dto.userId()).isEqualTo(20L);
        assertThat(dto.quantity()).isEqualTo(3);
        assertThat(dto.borrowDate()).isEqualTo(LocalDateTime.of(2025, 10, 1, 10, 0));
        assertThat(dto.expectedReturnDate()).isEqualTo(LocalDateTime.of(2025, 11, 1, 10, 0));
        assertThat(dto.actualReturnDate()).isEqualTo(LocalDateTime.of(2025, 10, 15, 14, 0));
        assertThat(dto.status()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(dto.purpose()).isEqualTo("Project X");
    }

    @Test
    void shouldUseDefaultsWhenBorrowingIsNull() {
        BorrowingDTO dto = new BorrowingDTO(null);
        assertThat(dto.id()).isNull();
        assertThat(dto.itemId()).isNull();
        assertThat(dto.userId()).isNull();
        assertThat(dto.quantity()).isEqualTo(1);
        assertThat(dto.borrowDate()).isNotNull();
        assertThat(dto.expectedReturnDate()).isNull();
        assertThat(dto.actualReturnDate()).isNull();
        assertThat(dto.status()).isEqualTo(BorrowStatus.ACTIVE);
        assertThat(dto.purpose()).isNull();
    }


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
    void shouldMapItemMaintenanceEntityToDTO() {
        ItemMaintenance m = new ItemMaintenance();
        m.setId(100L);
        Item item = new Item();
        item.setId(200L);
        m.setItem(item);
        User technician = new User();
        technician.setId(300L);
        m.setTechnician(technician);
        m.setMaintenanceDate(LocalDateTime.of(2025, 9, 1, 9, 0));
        m.setNextMaintenanceDate(LocalDateTime.of(2026, 9, 1, 9, 0));
        m.setCost(BigDecimal.valueOf(150.50));
        m.setDescription("Oil change");
        m.setStatus(MaintenanceStatus.COMPLETED);
        m.setCreatedAt(LocalDateTime.of(2025, 9, 1, 10, 0));

        ItemMaintenanceDTO dto = new ItemMaintenanceDTO(m);

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.itemId()).isEqualTo(200L);
        assertThat(dto.technicianId()).isEqualTo(300L);
        assertThat(dto.maintenanceDate()).isEqualTo(LocalDateTime.of(2025, 9, 1, 9, 0));
        assertThat(dto.nextMaintenanceDate()).isEqualTo(LocalDateTime.of(2026, 9, 1, 9, 0));
        assertThat(dto.cost()).isEqualByComparingTo("150.50");
        assertThat(dto.description()).isEqualTo("Oil change");
        assertThat(dto.status()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2025, 9, 1, 10, 0));
    }

    @Test
    void shouldHandleNullItemMaintenance() {
        ItemMaintenanceDTO dto = new ItemMaintenanceDTO(null);
        assertThat(dto.id()).isNull();
        assertThat(dto.itemId()).isNull();
        assertThat(dto.technicianId()).isNull();
        assertThat(dto.maintenanceDate()).isNull();
        assertThat(dto.nextMaintenanceDate()).isNull();
        assertThat(dto.cost()).isNull();
        assertThat(dto.description()).isNull();
        assertThat(dto.status()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(dto.createdAt()).isNull();
    }


    @Test
    void shouldMapKeepingEntityToDTO() {
        Keeping k = new Keeping();
        k.setId(77L);
        Storage storage = new Storage();
        storage.setId(88L);
        k.setStorage(storage);
        Item item = new Item();
        item.setId(99L);
        k.setItem(item);
        k.setQuantity(50);
        k.setShelf("A3-B2");
        k.setLastUpdated(LocalDateTime.of(2025, 10, 20, 14, 30));

        KeepingDTO dto = new KeepingDTO(k);

        assertThat(dto.id()).isEqualTo(77L);
        assertThat(dto.storageId()).isEqualTo(88L);
        assertThat(dto.itemId()).isEqualTo(99L);
        assertThat(dto.quantity()).isEqualTo(50);
        assertThat(dto.shelf()).isEqualTo("A3-B2");
        assertThat(dto.lastUpdated()).isEqualTo(LocalDateTime.of(2025, 10, 20, 14, 30));
    }

    @Test
    void shouldHandleNullKeeping() {
        KeepingDTO dto = new KeepingDTO(null);
        assertThat(dto.id()).isNull();
        assertThat(dto.storageId()).isNull();
        assertThat(dto.itemId()).isNull();
        assertThat(dto.quantity()).isEqualTo(1);
        assertThat(dto.shelf()).isNull();
        assertThat(dto.lastUpdated()).isNull();
    }

    @Test
    void shouldMapTransportationEntityToDTO() {
        Transportation t = new Transportation();
        t.setId(500L);
        Item item = new Item();
        item.setId(501L);
        t.setItem(item);
        Vehicle vehicle = new Vehicle();
        vehicle.setId(502L);
        t.setVehicle(vehicle);
        User driver = new User();
        driver.setId(503L);
        t.setDriver(driver);
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
    void shouldMapUserStorageAccessEntityToDTO() {
        UserStorageAccess a = new UserStorageAccess();
        a.setId(600L);
        User user = new User();
        user.setId(601L);
        a.setUser(user);
        Storage storage = new Storage();
        storage.setId(602L);
        a.setStorage(storage);
        a.setAccessLevel(AccessLevel.ADMIN);
        User grantedBy = new User();
        grantedBy.setId(603L);
        a.setGrantedBy(grantedBy);
        a.setGrantedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        a.setExpiresAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        a.setIsActive(true);

        UserStorageAccessDTO dto = new UserStorageAccessDTO(a);

        assertThat(dto.id()).isEqualTo(600L);
        assertThat(dto.userId()).isEqualTo(601L);
        assertThat(dto.storageId()).isEqualTo(602L);
        assertThat(dto.accessLevel()).isEqualTo(AccessLevel.ADMIN);
        assertThat(dto.grantedById()).isEqualTo(603L);
        assertThat(dto.grantedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(dto.expiresAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(dto.isActive()).isTrue();
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
    void shouldHandleNullUserStorageAccess() {
        UserStorageAccessDTO dto = new UserStorageAccessDTO(null);
        assertThat(dto.id()).isNull();
        assertThat(dto.userId()).isNull();
        assertThat(dto.storageId()).isNull();
        assertThat(dto.accessLevel()).isEqualTo(AccessLevel.BASIC);
        assertThat(dto.grantedById()).isNull();
        assertThat(dto.grantedAt()).isNull();
        assertThat(dto.expiresAt()).isNull();
        assertThat(dto.isActive()).isTrue();
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