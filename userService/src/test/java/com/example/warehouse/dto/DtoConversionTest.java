package com.example.warehouse.dto;

import com.example.warehouse.entity.*;
import com.example.warehouse.enumeration.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

public class DtoConversionTest {



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
}