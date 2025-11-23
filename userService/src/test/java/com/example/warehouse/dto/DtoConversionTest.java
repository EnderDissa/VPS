package com.example.warehouse.dto;

import com.example.warehouse.entity.*;
import com.example.warehouse.enumeration.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

public class DtoConversionTest {

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
}