package com.example.warehouse.dto;

import com.example.warehouse.domain.enumeration.BorrowStatus;
import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.domain.enumeration.ItemType;
import com.example.warehouse.domain.enumeration.MaintenanceStatus;
import com.example.warehouse.infrastructure.persistence.entity.Borrowing;
import com.example.warehouse.infrastructure.persistence.entity.Item;
import com.example.warehouse.infrastructure.persistence.entity.ItemMaintenance;
import com.example.warehouse.infrastructure.persistence.entity.Keeping;
import com.example.warehouse.infrastructure.web.dto.BorrowingDTO;
import com.example.warehouse.infrastructure.web.dto.ItemDTO;
import com.example.warehouse.infrastructure.web.dto.ItemMaintenanceDTO;
import com.example.warehouse.infrastructure.web.dto.KeepingDTO;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

public class DtoConversionTest {

    @Test
    void shouldMapBorrowingEntityToBorrowingDTO() {
        Item item = new Item();
        item.setId(10L);
        Borrowing borrowing = Borrowing.builder()
            .id(1L)
            .item(item)
            .userId(20L)
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
        m.setTechnicianId(300L);
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
        k.setStorageId(88L);
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
}