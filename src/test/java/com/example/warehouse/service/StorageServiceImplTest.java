package com.example.warehouse.service;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.mapper.StorageMapper;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import java.time.LocalDateTime;

import org.springframework.data.domain.*;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private KeepingRepository keepingRepository;

    @Mock
    private StorageMapper storageMapper;

    @InjectMocks
    private StorageServiceImpl storageService;

    private StorageDTO storageDTO;
    private Storage storage;

    @BeforeEach
    void setUp() {
        storageDTO = new StorageDTO(
                null,
                "Test Storage",
                "1234 Warehouse St",
                100,
                LocalDateTime.now()
        );

        storage = new Storage();
        storage.setId(1L);
        storage.setName("Test Storage");
        storage.setAddress("1234 Warehouse St");
        storage.setCapacity(100);
        storage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateStorage_Success() {
        when(storageRepository.existsByName(storageDTO.name())).thenReturn(false);
        when(storageMapper.toEntity(storageDTO)).thenReturn(storage);
        when(storageRepository.save(storage)).thenReturn(storage);
        when(storageMapper.toDTO(storage)).thenReturn(storageDTO);

        StorageDTO createdStorage = storageService.create(storageDTO);

        assertThat(createdStorage).isNotNull();
        assertThat(createdStorage.name()).isEqualTo("Test Storage");
        verify(storageRepository, times(1)).save(storage);
    }

    @Test
    void testCreateStorage_DuplicateName() {
        when(storageRepository.existsByName(storageDTO.name())).thenReturn(true);

        assertThatThrownBy(() -> storageService.create(storageDTO))
                .isInstanceOf(DuplicateStorageException.class)
                .hasMessageContaining("Storage with name 'Test Storage' already exists");
    }

    @Test
    void testGetStorageById_Success() {
        when(storageRepository.findById(1L)).thenReturn(java.util.Optional.of(storage));
        when(storageMapper.toDTO(storage)).thenReturn(storageDTO);

        StorageDTO foundStorage = storageService.getById(1L);

        assertThat(foundStorage).isNotNull();
        assertThat(foundStorage.name()).isEqualTo("Test Storage");
    }

    @Test
    void testGetStorageById_NotFound() {
        when(storageRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> storageService.getById(1L))
                .isInstanceOf(StorageNotFoundException.class)
                .hasMessageContaining("Storage not found with ID: 1");
    }

    @Test
    void testUpdateStorage_Success() {
        when(storageRepository.findById(1L)).thenReturn(java.util.Optional.of(storage));
        storageService.update(1L, storageDTO);

        verify(storageRepository, times(1)).save(storage);
    }

    @Test
    void testUpdateStorage_DuplicateName() {
        StorageDTO updatedDTO = new StorageDTO(
                1L, "Duplicate Storage", "1234 Warehouse St", 100, LocalDateTime.now()
        );

        when(storageRepository.findById(1L)).thenReturn(java.util.Optional.of(storage));
        when(storageRepository.existsByName(updatedDTO.name())).thenReturn(true);

        assertThatThrownBy(() -> storageService.update(1L, updatedDTO))
                .isInstanceOf(DuplicateStorageException.class)
                .hasMessageContaining("Storage with name 'Duplicate Storage' already exists");
    }

    @Test
    void testDeleteStorage_Success() {
        when(storageRepository.findById(1L)).thenReturn(java.util.Optional.of(storage));
        when(keepingRepository.countByStorageId(1L)).thenReturn(0L);

        storageService.delete(1L);

        verify(storageRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteStorage_NotEmpty() {
        when(storageRepository.findById(1L)).thenReturn(java.util.Optional.of(storage));
        when(keepingRepository.countByStorageId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> storageService.delete(1L))
                .isInstanceOf(StorageNotEmptyException.class)
                .hasMessageContaining("Cannot delete storage with ID: 1. It contains 5 items.");
    }

    @Test
    void testFindPage() {
        Page<Storage> page = new PageImpl<>(Collections.singletonList(storage));
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(storageRepository.findByNameContainingIgnoreCase("Test", pageable)).thenReturn(page);

        Page<StorageDTO> resultPage = storageService.findPage(0, 10, "Test");

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(1);
    }
}
