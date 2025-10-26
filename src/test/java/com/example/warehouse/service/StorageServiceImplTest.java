package com.example.warehouse.service;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.mapper.StorageMapper;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageServiceImplTest {

    private StorageRepository repository;
    private KeepingRepository keepingRepository;
    private StorageMapper mapper;
    private StorageServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(StorageRepository.class);
        keepingRepository = mock(KeepingRepository.class);
        mapper = mock(StorageMapper.class);
        service = new StorageServiceImpl(repository, keepingRepository, mapper);
    }

    @Test
    void getById_ok() {
        Storage s = new Storage(); s.setId(11L);
        when(repository.findById(11L)).thenReturn(Optional.of(s));

        StorageDTO dto = new StorageDTO(11L, "Main", "City A", 100, LocalDateTime.now());
        when(mapper.toDTO(s)).thenReturn(dto);

        var rs = service.getById(11L);
        assertThat(rs.id()).isEqualTo(11L);
    }

    @Test
    void getById_notFound() {
        when(repository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(404L)).isInstanceOf(IllegalArgumentException.class);
    }
}
