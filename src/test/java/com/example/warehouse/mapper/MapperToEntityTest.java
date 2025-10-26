package com.example.warehouse.mapper;

import com.example.warehouse.dto.*;
import com.example.warehouse.entity.*;
import com.example.warehouse.enumeration.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class MapperToEntityTest {

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemMaintenanceMapper itemMaintenanceMapper;

    @Autowired
    private KeepingMapper keepingMapper;

    @Autowired
    private TransportationMapper transportationMapper;

    @Autowired
    private UserStorageAccessMapper userStorageAccessMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private StorageMapper storageMapper;

    // ===== BorrowingMapper =====

    @Test
    void shouldMapBorrowingDTOToEntity() {
        BorrowingDTO dto = new BorrowingDTO(
                1L,
                10L, // itemId
                20L, // userId
                3,
                LocalDateTime.of(2025, 10, 1, 10, 0),
                LocalDateTime.of(2025, 11, 1, 10, 0),
                LocalDateTime.of(2025, 10, 15, 14, 0),
                BorrowStatus.RETURNED,
                "Project X"
        );

        Borrowing entity = borrowingMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getQuantity()).isEqualTo(3);
        assertThat(entity.getBorrowDate()).isEqualTo(LocalDateTime.of(2025, 10, 1, 10, 0));
        assertThat(entity.getExpectedReturnDate()).isEqualTo(LocalDateTime.of(2025, 11, 1, 10, 0));
        assertThat(entity.getActualReturnDate()).isEqualTo(LocalDateTime.of(2025, 10, 15, 14, 0));
        assertThat(entity.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(entity.getPurpose()).isEqualTo("Project X");

        // Note: item and user are NOT set from itemId/userId in current mapper
        assertThat(entity.getItem()).isNull();
        assertThat(entity.getUser()).isNull();
    }

    @Test
    void shouldReturnNullWhenBorrowingDTOIsNull() {
        assertThat(borrowingMapper.toEntity(null)).isNull();
    }

    // ===== ItemMapper =====

    @Test
    void shouldMapItemDTOToEntity() {
        ItemDTO dto = new ItemDTO(
                5L,
                "Laptop",
                ItemType.ELECTRONICS,
                ItemCondition.NEW,
                "SN12345",
                "High-end laptop",
                LocalDateTime.of(2024, 1, 1, 0, 0)
        );

        Item entity = itemMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getName()).isEqualTo("Laptop");
        assertThat(entity.getType()).isEqualTo(ItemType.ELECTRONICS);
        assertThat(entity.getCondition()).isEqualTo(ItemCondition.NEW);
        assertThat(entity.getSerialNumber()).isEqualTo("SN12345");
        assertThat(entity.getDescription()).isEqualTo("High-end laptop");
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    void shouldReturnNullWhenItemDTOIsNull() {
        assertThat(itemMapper.toEntity(null)).isNull();
    }

    // ===== ItemMaintenanceMapper =====

    @Test
    void shouldMapItemMaintenanceDTOToEntity() {
        ItemMaintenanceDTO dto = new ItemMaintenanceDTO(
                100L,
                200L, // itemId
                300L, // technicianId
                LocalDateTime.of(2025, 9, 1, 9, 0),
                LocalDateTime.of(2026, 9, 1, 9, 0),
                BigDecimal.valueOf(150.50),
                "Oil change",
                MaintenanceStatus.COMPLETED,
                LocalDateTime.of(2025, 9, 1, 10, 0)
        );

        ItemMaintenance entity = itemMaintenanceMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(100L);
        assertThat(entity.getMaintenanceDate()).isEqualTo(LocalDateTime.of(2025, 9, 1, 9, 0));
        assertThat(entity.getNextMaintenanceDate()).isEqualTo(LocalDateTime.of(2026, 9, 1, 9, 0));
        assertThat(entity.getCost()).isEqualByComparingTo("150.50");
        assertThat(entity.getDescription()).isEqualTo("Oil change");
        assertThat(entity.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 9, 1, 10, 0));

        // Relationships not set from IDs
        assertThat(entity.getItem()).isNull();
        assertThat(entity.getTechnician()).isNull();
    }

    @Test
    void shouldReturnNullWhenItemMaintenanceDTOIsNull() {
        assertThat(itemMaintenanceMapper.toEntity(null)).isNull();
    }

    // ===== KeepingMapper =====

    @Test
    void shouldMapKeepingDTOToEntity() {
        KeepingDTO dto = new KeepingDTO(
                77L,
                88L, // storageId
                99L, // itemId
                50,
                "A3-B2",
                LocalDateTime.of(2025, 10, 20, 14, 30)
        );

        Keeping entity = keepingMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(77L);
        assertThat(entity.getQuantity()).isEqualTo(50);
        assertThat(entity.getShelf()).isEqualTo("A3-B2");
        assertThat(entity.getLastUpdated()).isEqualTo(LocalDateTime.of(2025, 10, 20, 14, 30));

        // Relationships not set
        assertThat(entity.getStorage()).isNull();
        assertThat(entity.getItem()).isNull();
    }

    @Test
    void shouldReturnNullWhenKeepingDTOIsNull() {
        assertThat(keepingMapper.toEntity(null)).isNull();
    }

    // ===== TransportationMapper =====

    @Test
    void shouldMapTransportationDTOToEntity() {
        TransportationDTO dto = new TransportationDTO(
                500L,
                501L, // itemId
                502L, // vehicleId
                503L, // driverId
                504L, // fromStorageId
                505L, // toStorageId
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

        // Relationships not set
        assertThat(entity.getItem()).isNull();
        assertThat(entity.getVehicle()).isNull();
        assertThat(entity.getDriver()).isNull();
        assertThat(entity.getFromStorage()).isNull();
        assertThat(entity.getToStorage()).isNull();
    }

    @Test
    void shouldReturnNullWhenTransportationDTOIsNull() {
        assertThat(transportationMapper.toEntity(null)).isNull();
    }

    // ===== UserStorageAccessMapper =====

    @Test
    void shouldMapUserStorageAccessDTOToEntity() {
        UserStorageAccessDTO dto = new UserStorageAccessDTO(
                600L,
                601L, // userId
                602L, // storageId
                AccessLevel.ADMIN,
                603L, // grantedById
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                true
        );

        UserStorageAccess entity = userStorageAccessMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(600L);
        assertThat(entity.getAccessLevel()).isEqualTo(AccessLevel.ADMIN);
        assertThat(entity.getGrantedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(entity.getExpiresAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(entity.getIsActive()).isTrue();

        // Relationships not set
        assertThat(entity.getUser()).isNull();
        assertThat(entity.getStorage()).isNull();
        assertThat(entity.getGrantedBy()).isNull();
    }

    @Test
    void shouldReturnNullWhenUserStorageAccessDTOIsNull() {
        assertThat(userStorageAccessMapper.toEntity(null)).isNull();
    }

    // ===== VehicleMapper =====

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

    // ===== StorageMapper =====

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