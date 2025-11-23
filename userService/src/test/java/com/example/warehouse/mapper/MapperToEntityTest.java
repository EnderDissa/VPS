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
    private UserStorageAccessMapper userStorageAccessMapper;

    @Test
    void shouldMapUserStorageAccessDTOToEntity() {
        UserStorageAccessDTO dto = new UserStorageAccessDTO(
                600L,
                601L,
                602L,
                AccessLevel.ADMIN,
                603L,
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

        assertThat(entity.getUserId()).isNotNull();
        assertThat(entity.getStorageId()).isNotNull();
        assertThat(entity.getGrantedById()).isNotNull();
    }

    @Test
    void shouldReturnNullWhenUserStorageAccessDTOIsNull() {
        assertThat(userStorageAccessMapper.toEntity(null)).isNull();
    }
}