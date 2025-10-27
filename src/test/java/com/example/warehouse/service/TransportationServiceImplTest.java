package com.example.warehouse.service;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;
import com.example.warehouse.mapper.TransportationMapper;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.repository.TransportationRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransportationServiceImplTest {

    private TransportationRepository repository;
    private ItemRepository itemRepository;
    private VehicleRepository vehicleRepository;
    private UserRepository userRepository;
    private StorageRepository storageRepository;
    private TransportationMapper mapper;
    private TransportationServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(TransportationRepository.class);
        itemRepository = mock(ItemRepository.class);
        vehicleRepository = mock(VehicleRepository.class);
        userRepository = mock(UserRepository.class);
        storageRepository = mock(StorageRepository.class);
        mapper = mock(TransportationMapper.class);
        service = new TransportationServiceImpl(repository, itemRepository, vehicleRepository, userRepository, storageRepository, mapper);
    }

    @Test
    void getById_ok() {
        Transportation t = new Transportation(); t.setId(77L);
        when(repository.findById(77L)).thenReturn(Optional.of(t));
        when(mapper.toDTO(t)).thenReturn(new TransportationDTO(t));

        var rs = service.getById(77L);
        assertThat(rs).isNotNull();
    }

    @Test
    void getById_notFound() {
        when(repository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(7L)).isInstanceOf(IllegalArgumentException.class);
    }
}
