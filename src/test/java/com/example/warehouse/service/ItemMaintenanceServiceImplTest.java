package com.example.warehouse.service;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.mapper.ItemMaintenanceMapper;
import com.example.warehouse.repository.ItemMaintenanceRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemMaintenanceServiceImplTest {

    private ItemMaintenanceRepository repository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private ItemMaintenanceMapper mapper;
    private ItemMaintenanceServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ItemMaintenanceRepository.class);
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        mapper = mock(ItemMaintenanceMapper.class);
        service = new ItemMaintenanceServiceImpl(repository, itemRepository, userRepository, mapper);
    }

    @Test
    void getById_ok() {
        ItemMaintenance im = new ItemMaintenance(); im.setId(2L);
        when(repository.findById(2L)).thenReturn(Optional.of(im));
        when(mapper.toDTO(im)).thenReturn(new ItemMaintenanceDTO(im));

        var rs = service.getById(2L);
        assertThat(rs).isNotNull();
    }

    @Test
    void getById_notFound() {
        when(repository.findById(0L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(0L)).isInstanceOf(IllegalArgumentException.class);
    }
}
