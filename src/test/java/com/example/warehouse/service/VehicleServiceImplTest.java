package com.example.warehouse.service;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.mapper.VehicleMapper;
import com.example.warehouse.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleServiceImplTest {

    private VehicleRepository repository;
    private VehicleMapper mapper;
    private VehicleServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(VehicleRepository.class);
        mapper = mock(VehicleMapper.class);
        service = new VehicleServiceImpl(repository, mapper);
    }

    @Test
    void getById_notFound_illegalArgument() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findPage_filters_ok() {
        Vehicle v1 = new Vehicle(); v1.setId(1L);
        Vehicle v2 = new Vehicle(); v2.setId(2L);
        Page<Vehicle> page = new PageImpl<>(List.of(v1, v2));

        when(repository.findAll(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toDTO(v1)).thenReturn(new VehicleDTO(v1));
        when(mapper.toDTO(v2)).thenReturn(new VehicleDTO(v2));

        var rs = service.findPage(0, 10, VehicleStatus.AVAILABLE, "VW", "Crafter");

        assertThat(rs.getContent()).hasSize(2);
        verify(repository).findAll(any(), any(Pageable.class));
    }
}
