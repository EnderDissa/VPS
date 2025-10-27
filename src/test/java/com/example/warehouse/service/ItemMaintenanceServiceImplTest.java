package com.example.warehouse.service;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.MaintenanceStatus;
import com.example.warehouse.exception.ItemMaintenanceNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.mapper.ItemMaintenanceMapper;
import com.example.warehouse.repository.ItemMaintenanceRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

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
    void testDeleteItemMaintenance_Success() {
        ItemMaintenance itemMaintenance = new ItemMaintenance();
        itemMaintenance.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(itemMaintenance));
        doNothing().when(repository).deleteById(1L);

        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteItemMaintenance_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ItemMaintenanceNotFoundException.class)
                .hasMessageContaining("Item maintenance not found with ID: 1");
    }

    @Test
    void testFindPage_Success() {
        ItemMaintenance maintenance = new ItemMaintenance();
        maintenance.setId(1L);
        maintenance.setItem(new Item());
        maintenance.setTechnician(new User());
        maintenance.setMaintenanceDate(LocalDateTime.now());
        maintenance.setNextMaintenanceDate(LocalDateTime.now().plusMonths(6));
        maintenance.setCost(BigDecimal.valueOf(100));

        Page<ItemMaintenance> page = new PageImpl<>(Collections.singletonList(maintenance));
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "maintenanceDate"));

        when(repository.findAll(pageable)).thenReturn(page);

        Page<ItemMaintenanceDTO> resultPage = service.findPage(0, 10, null, null);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(1);
    }

    @Test
    void testFindPage_ItemNotFound() {
        Page<ItemMaintenance> page = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "maintenanceDate"));

        when(repository.findAll(pageable)).thenReturn(page);

        Page<ItemMaintenanceDTO> resultPage = service.findPage(0, 10, 1L, null);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).isEmpty();
    }
}
