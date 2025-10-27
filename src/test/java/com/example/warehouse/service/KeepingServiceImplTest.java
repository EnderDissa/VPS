package com.example.warehouse.service;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;
import com.example.warehouse.exception.KeepingNotFoundException;
import com.example.warehouse.mapper.KeepingMapper;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeepingServiceImplTest {

    private KeepingRepository repository;
    private StorageRepository storageRepository;
    private ItemRepository itemRepository;
    private KeepingMapper mapper;
    private KeepingServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(KeepingRepository.class);
        storageRepository = mock(StorageRepository.class);
        itemRepository = mock(ItemRepository.class);
        mapper = mock(KeepingMapper.class);
        service = new KeepingServiceImpl(repository, storageRepository, itemRepository, mapper);
    }

    @Test
    void getById_ok() {
        Keeping k = new Keeping(); k.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(k));
        when(mapper.toDTO(k)).thenReturn(new KeepingDTO(k));

        var rs = service.getById(5L);
        assertThat(rs).isNotNull();
    }

    @Test
    void getById_notFound() {
        when(repository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(123L)).isInstanceOf(KeepingNotFoundException.class);
    }
}
