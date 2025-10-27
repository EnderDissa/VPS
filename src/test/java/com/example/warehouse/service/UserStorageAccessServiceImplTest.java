package com.example.warehouse.service;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.mapper.UserStorageAccessMapper;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.repository.UserStorageAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserStorageAccessServiceImplTest {

    private UserStorageAccessRepository repository;
    private UserRepository userRepository;
    private StorageRepository storageRepository;
    private UserStorageAccessMapper mapper;
    private UserStorageAccessServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(UserStorageAccessRepository.class);
        userRepository = mock(UserRepository.class);
        storageRepository = mock(StorageRepository.class);
        mapper = mock(UserStorageAccessMapper.class);
        service = new UserStorageAccessServiceImpl(repository, userRepository, storageRepository, mapper);
    }

    @Test
    void getById_ok() {
        UserStorageAccess usa = new UserStorageAccess(); usa.setId(9L);
        when(repository.findById(9L)).thenReturn(Optional.of(usa));
        when(mapper.toDTO(usa)).thenReturn(new UserStorageAccessDTO(usa));

        var rs = service.getById(9L);
        assertThat(rs).isNotNull();
    }

    @Test
    void getById_notFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(IllegalArgumentException.class);
    }
}
