package com.example.warehouse.service;

import com.example.warehouse.entity.Storage;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageServiceImpl storageService;

    private Storage testStorage1;
    private Storage testStorage2;
    private Storage testStorage3;

    @BeforeEach
    void setUp() {
        testStorage1 = Storage.builder()
                .id(1L)
                .name("Main Warehouse")
                .address("123 Main Street, City Center")
                .capacity(1000)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        testStorage2 = Storage.builder()
                .id(2L)
                .name("Secondary Storage")
                .address("456 Oak Avenue, Industrial Zone")
                .capacity(500)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        testStorage3 = Storage.builder()
                .id(3L)
                .name("Tertiary Warehouse")
                .address("789 Pine Road, Suburb")
                .capacity(300)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void create_ShouldCreateStorage_WhenValidData() {
        // Arrange
        Storage newStorage = Storage.builder()
                .name("New Storage Facility")
                .address("999 New Street, Business Park")
                .capacity(750)
                .createdAt(LocalDateTime.now())
                .build();

        Storage savedStorage = Storage.builder()
                .id(4L)
                .name("New Storage Facility")
                .address("999 New Street, Business Park")
                .capacity(750)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageRepository.existsByName("New Storage Facility")).thenReturn(false);
        when(storageRepository.save(newStorage)).thenReturn(savedStorage);

        // Act & Assert
        StepVerifier.create(storageService.create(newStorage))
                .expectNextMatches(storage -> {
                    assertEquals(4L, storage.getId());
                    assertEquals("New Storage Facility", storage.getName());
                    assertEquals("999 New Street, Business Park", storage.getAddress());
                    assertEquals(750, storage.getCapacity());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).existsByName("New Storage Facility");
        verify(storageRepository).save(newStorage);
    }

    @Test
    void create_ShouldCreateStorage_WhenZeroCapacity() {
        // Arrange
        Storage newStorage = Storage.builder()
                .name("Zero Capacity Storage")
                .address("Zero Address")
                .capacity(0)
                .createdAt(LocalDateTime.now())
                .build();

        Storage savedStorage = Storage.builder()
                .id(5L)
                .name("Zero Capacity Storage")
                .address("Zero Address")
                .capacity(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageRepository.existsByName("Zero Capacity Storage")).thenReturn(false);
        when(storageRepository.save(newStorage)).thenReturn(savedStorage);

        // Act & Assert
        StepVerifier.create(storageService.create(newStorage))
                .expectNextMatches(storage -> {
                    assertEquals(0, storage.getCapacity());
                    assertEquals("Zero Capacity Storage", storage.getName());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void create_ShouldThrowDuplicateStorageException_WhenNameExists() {
        // Arrange
        Storage duplicateStorage = Storage.builder()
                .name("Main Warehouse")
                .address("Different Address")
                .capacity(200)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageRepository.existsByName("Main Warehouse")).thenReturn(true);

        // Act & Assert
        StepVerifier.create(storageService.create(duplicateStorage))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateStorageException &&
                                throwable.getMessage().contains("Storage with name 'Main Warehouse' already exists")
                )
                .verify();

        verify(storageRepository).existsByName("Main Warehouse");
        verify(storageRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnStorage_WhenStorageExists() {
        // Arrange
        when(storageRepository.findById(1L)).thenReturn(Optional.of(testStorage1));

        // Act & Assert
        StepVerifier.create(storageService.getById(1L))
                .expectNextMatches(storage -> {
                    assertEquals(testStorage1.getId(), storage.getId());
                    assertEquals(testStorage1.getName(), storage.getName());
                    assertEquals(testStorage1.getAddress(), storage.getAddress());
                    assertEquals(testStorage1.getCapacity(), storage.getCapacity());
                    assertEquals(testStorage1.getCreatedAt(), storage.getCreatedAt());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
        // Arrange
        when(storageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(storageService.getById(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof StorageNotFoundException &&
                                throwable.getMessage().contains("Storage not found with ID: 999")
                )
                .verify();

        verify(storageRepository).findById(999L);
    }

    @Test
    void update_ShouldUpdateStorage_WhenValidData() {
        // Arrange
        Storage updateData = Storage.builder()
                .name("Updated Main Warehouse")
                .address("Updated Address, New Location")
                .capacity(1200)
                .build();

        when(storageRepository.findById(1L)).thenReturn(Optional.of(testStorage1));
        when(storageRepository.existsByName("Updated Main Warehouse")).thenReturn(false);
        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(storageService.update(1L, updateData))
                .expectNextMatches(updated -> {
                    assertEquals("Updated Main Warehouse", updated.getName());
                    assertEquals("Updated Address, New Location", updated.getAddress());
                    assertEquals(1200, updated.getCapacity());
                    assertEquals(testStorage1.getCreatedAt(), updated.getCreatedAt());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findById(1L);
        verify(storageRepository).existsByName("Updated Main Warehouse");
        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void update_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        Storage updateData = Storage.builder()
                .name("Non-existent Storage")
                .address("Address")
                .capacity(100)
                .build();

        when(storageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(storageService.update(nonExistentId, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof StorageNotFoundException &&
                                throwable.getMessage().contains("Storage not found with ID: " + nonExistentId)
                )
                .verify();

        verify(storageRepository).findById(nonExistentId);
        verify(storageRepository, never()).existsByName(anyString());
        verify(storageRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowDuplicateStorageException_WhenNameTakenByOtherStorage() {
        // Arrange
        Storage updateData = Storage.builder()
                .name("Secondary Storage")
                .address(testStorage1.getAddress())
                .capacity(testStorage1.getCapacity())
                .build();

        when(storageRepository.findById(1L)).thenReturn(Optional.of(testStorage1));
        when(storageRepository.existsByName("Secondary Storage")).thenReturn(true);

        // Act & Assert
        StepVerifier.create(storageService.update(1L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateStorageException &&
                                throwable.getMessage().contains("Storage with name 'Secondary Storage' already exists")
                )
                .verify();

        verify(storageRepository).findById(1L);
        verify(storageRepository).existsByName("Secondary Storage");
        verify(storageRepository, never()).save(any());
    }

    @Test
    void update_ShouldNotThrowException_WhenNameNotChanged() {
        // Arrange
        Storage updateData = Storage.builder()
                .name(testStorage1.getName())
                .address("Updated Address")
                .capacity(1500)
                .build();

        when(storageRepository.findById(1L)).thenReturn(Optional.of(testStorage1));
        // existsByName не вызывается, так как имя не меняется
        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(storageService.update(1L, updateData))
                .expectNextMatches(updated -> {
                    assertEquals(testStorage1.getName(), updated.getName());
                    assertEquals("Updated Address", updated.getAddress());
                    assertEquals(1500, updated.getCapacity());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findById(1L);
        verify(storageRepository, never()).existsByName(anyString());
        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void delete_ShouldDeleteStorage_WhenStorageExistsAndEmpty() {
        // Arrange
        when(storageRepository.findById(2L)).thenReturn(Optional.of(testStorage2));
        when(storageRepository.countKeepingsByStorageId(2L)).thenReturn(0L);
        doNothing().when(storageRepository).deleteById(2L);

        // Act & Assert
        StepVerifier.create(storageService.delete(2L))
                .verifyComplete();

        verify(storageRepository).findById(2L);
        verify(storageRepository).countKeepingsByStorageId(2L);
        verify(storageRepository).deleteById(2L);
    }

    @Test
    void delete_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(storageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(storageService.delete(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof StorageNotFoundException &&
                                throwable.getMessage().contains("Storage not found with ID: " + nonExistentId)
                )
                .verify();

        verify(storageRepository).findById(nonExistentId);
        verify(storageRepository, never()).countKeepingsByStorageId(anyLong());
        verify(storageRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_ShouldThrowStorageNotEmptyException_WhenStorageHasItems() {
        // Arrange
        when(storageRepository.findById(1L)).thenReturn(Optional.of(testStorage1));
        when(storageRepository.countKeepingsByStorageId(1L)).thenReturn(1L);

        // Act & Assert
        StepVerifier.create(storageService.delete(1L))
                .expectErrorMatches(throwable ->
                        throwable instanceof StorageNotEmptyException &&
                                throwable.getMessage().contains("Cannot delete storage with ID: " + testStorage1.getId()) &&
                                throwable.getMessage().contains("It contains 1 items.")
                )
                .verify();

        verify(storageRepository).findById(1L);
        verify(storageRepository).countKeepingsByStorageId(1L);
        verify(storageRepository, never()).deleteById(anyLong());
    }

    @Test
    void findPage_ShouldReturnAllStorages_WhenNoNameFilter() {
        // Arrange
        List<Storage> storages = List.of(testStorage3, testStorage2, testStorage1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Storage> page = new PageImpl<>(storages, pageable, storages.size());

        when(storageRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(storageService.findPage(0, 10, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertEquals(3, result.getContent().size());
                    // Проверяем сортировку по createdAt DESC
                    assertEquals(testStorage3.getId(), result.getContent().get(0).getId());
                    assertEquals(testStorage2.getId(), result.getContent().get(1).getId());
                    assertEquals(testStorage1.getId(), result.getContent().get(2).getId());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findAll(pageable);
    }

    @Test
    void findPage_ShouldReturnAllStorages_WhenEmptyNameFilter() {
        // Arrange
        List<Storage> storages = List.of(testStorage3, testStorage2, testStorage1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Storage> page = new PageImpl<>(storages, pageable, storages.size());

        when(storageRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(storageService.findPage(0, 10, ""))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findAll(pageable);
    }

    @Test
    void findPage_ShouldReturnFilteredStorages_WhenNameFilterApplied() {
        // Arrange
        String searchTerm = "warehouse";
        List<Storage> filteredStorages = List.of(testStorage3, testStorage1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Storage> page = new PageImpl<>(filteredStorages, pageable, filteredStorages.size());

        when(storageRepository.findByNameContainingIgnoreCase(searchTerm, pageable))
                .thenReturn(page);
        when(storageRepository.countByNameContainingIgnoreCase(searchTerm))
                .thenReturn((long) filteredStorages.size());

        // Act & Assert
        StepVerifier.create(storageService.findPage(0, 10, searchTerm))
                .expectNextMatches(result -> {
                    assertEquals(2, result.getTotalElements());
                    assertTrue(result.getContent().stream()
                            .allMatch(storage -> storage.getName().toLowerCase().contains("warehouse")));
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findByNameContainingIgnoreCase(searchTerm, pageable);
        verify(storageRepository).countByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatchingName() {
        // Arrange
        String searchTerm = "nonexistent";
        List<Storage> emptyList = List.of();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Storage> page = new PageImpl<>(emptyList, pageable, 0);

        when(storageRepository.findByNameContainingIgnoreCase(searchTerm, pageable))
                .thenReturn(page);
        when(storageRepository.countByNameContainingIgnoreCase(searchTerm))
                .thenReturn(0L);

        // Act & Assert
        StepVerifier.create(storageService.findPage(0, 10, searchTerm))
                .expectNextMatches(result -> {
                    assertEquals(0, result.getTotalElements());
                    assertTrue(result.getContent().isEmpty());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findByNameContainingIgnoreCase(searchTerm, pageable);
        verify(storageRepository).countByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
        // Arrange
        List<Storage> firstPageStorages = List.of(testStorage3, testStorage2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Storage> page = new PageImpl<>(firstPageStorages, pageable, 3);

        when(storageRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(storageService.findPage(0, 2, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertEquals(2, result.getContent().size());
                    assertEquals(2, result.getTotalPages());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findAll(pageable);
    }

    @Test
    void findPage_ShouldReturnSecondPage_WhenPageOneRequested() {
        // Arrange
        List<Storage> secondPageStorages = List.of(testStorage1);
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Storage> page = new PageImpl<>(secondPageStorages, pageable, 3);

        when(storageRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(storageService.findPage(1, 2, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertEquals(1, result.getContent().size());
                    assertEquals(1, result.getNumber()); // Номер страницы (0-based)
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findAll(pageable);
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
        // Arrange
        List<Storage> emptyList = List.of();
        Pageable pageable = PageRequest.of(10, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Storage> page = new PageImpl<>(emptyList, pageable, 3);

        when(storageRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(storageService.findPage(10, 10, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertTrue(result.getContent().isEmpty());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository).findAll(pageable);
    }

    @Test
    void create_ShouldHandleLongNamesAndAddresses() {
        // Arrange
        String longName = "A".repeat(255);
        String longAddress = "B".repeat(1000);
        Storage newStorage = Storage.builder()
                .name(longName)
                .address(longAddress)
                .capacity(100)
                .createdAt(LocalDateTime.now())
                .build();

        Storage savedStorage = Storage.builder()
                .id(6L)
                .name(longName)
                .address(longAddress)
                .capacity(100)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageRepository.existsByName(longName)).thenReturn(false);
        when(storageRepository.save(newStorage)).thenReturn(savedStorage);

        // Act & Assert
        StepVerifier.create(storageService.create(newStorage))
                .expectNextMatches(storage -> {
                    assertEquals(255, storage.getName().length());
                    assertEquals(1000, storage.getAddress().length());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void delete_ShouldWorkWhenStorageHasNoItems() {
        // Arrange
        Storage emptyStorage = Storage.builder()
                .id(7L)
                .name("Empty Storage")
                .address("Address")
                .capacity(100)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageRepository.findById(7L)).thenReturn(Optional.of(emptyStorage));
        when(storageRepository.countKeepingsByStorageId(7L)).thenReturn(0L);
        doNothing().when(storageRepository).deleteById(7L);

        // Act & Assert
        StepVerifier.create(storageService.delete(7L))
                .verifyComplete();

        verify(storageRepository).deleteById(7L);
    }

    @Test
    void getById_ShouldReturnConsistentData() {
        // Arrange
        when(storageRepository.findById(1L))
                .thenReturn(Optional.of(testStorage1))
                .thenReturn(Optional.of(testStorage1));

        // Act & Assert - первый вызов
        StepVerifier.create(storageService.getById(1L))
                .expectNextMatches(storage -> {
                    assertEquals(1L, storage.getId());
                    return true;
                })
                .verifyComplete();

        // Act & Assert - второй вызов
        StepVerifier.create(storageService.getById(1L))
                .expectNextMatches(storage -> {
                    assertEquals(1L, storage.getId());
                    return true;
                })
                .verifyComplete();

        verify(storageRepository, times(2)).findById(1L);
    }
}