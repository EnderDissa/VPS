package com.example.warehouse.service;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.enumeration.AccessLevel;
import com.example.warehouse.exception.UserStorageAccessNotFoundException;
import com.example.warehouse.exception.DuplicateUserStorageAccessException;
import com.example.warehouse.mapper.UserStorageAccessMapper;
import com.example.warehouse.repository.UserStorageAccessRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Collections;
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
        UserStorageAccess access = new UserStorageAccess();
        access.setId(9L);
        when(repository.findById(9L)).thenReturn(Optional.of(access));
        when(mapper.toDTO(access)).thenReturn(new UserStorageAccessDTO(access));

        var result = service.getById(9L);
        assertThat(result).isNotNull();
    }

    @Test
    void getById_notFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(9L))
                .isInstanceOf(UserStorageAccessNotFoundException.class)
                .hasMessageContaining("User storage access not found with ID: 9");
    }

    @Test
    void testDeleteTransportation_Success() {
        // Создаем mock объект для UserStorageAccess
        UserStorageAccess access = new UserStorageAccess();
        access.setId(1L);

        // Мокаем репозиторий, чтобы findById возвращал наш объект с нужным ID
        when(repository.findById(1L)).thenReturn(Optional.of(access));

        // Мокаем deleteById, чтобы он ничего не делал
        doNothing().when(repository).deleteById(1L);

        // Вызываем delete
        service.delete(1L);

        // Проверяем, что deleteById был вызван 1 раз
        verify(repository, times(1)).deleteById(1L);
    }


    @Test
    void testDeleteTransportation_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(UserStorageAccessNotFoundException.class)
                .hasMessageContaining("User storage access not found with ID: 1");
    }
}
