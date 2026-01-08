package com.example.warehouse.service;

import com.example.warehouse.client.StorageServiceClient;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.Keeping;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.exception.DuplicateKeepingException;
import com.example.warehouse.exception.KeepingNotFoundException;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.service.interfaces.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeepingServiceImplTest {

    @Mock
    private KeepingRepository keepingRepository;

    @Mock
    private StorageServiceClient storageService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private KeepingServiceImpl keepingService;

    private Storage testStorage1;
    private Storage testStorage2;
    private Item testItem1;
    private Item testItem2;
    private Keeping testKeeping1;
    private Keeping testKeeping2;

    @BeforeEach
    void setUp() {
        testStorage1 = Storage.builder()
                .id(1L)
                .name("Main Warehouse")
                .address("123 Main St")
                .capacity(1000)
                .createdAt(LocalDateTime.now())
                .build();

        testStorage2 = Storage.builder()
                .id(2L)
                .name("Secondary Storage")
                .address("456 Oak Ave")
                .capacity(500)
                .createdAt(LocalDateTime.now())
                .build();

        testItem1 = Item.builder()
                .id(1L)
                .name("Laptop Dell XPS")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .description("High-performance laptop")
                .createdAt(LocalDateTime.now())
                .build();

        testItem2 = Item.builder()
                .id(2L)
                .name("Office Chair")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.NEW)
                .serialNumber("SN789012")
                .description("Ergonomic office chair")
                .createdAt(LocalDateTime.now())
                .build();

        testKeeping1 = Keeping.builder()
                .id(1L)
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(5)
                .shelf("A1")
                .lastUpdated(LocalDateTime.now().minusDays(2))
                .build();

        testKeeping2 = Keeping.builder()
                .id(2L)
                .storageId(testStorage2.getId())
                .item(testItem2)
                .quantity(10)
                .shelf("B2")
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void create_ShouldCreateKeeping_WhenValidData() {
        Keeping newKeeping = Keeping.builder()
                .storageId(testStorage1.getId())
                .item(testItem2)
                .quantity(15)
                .shelf("C3")
                .build();

        Keeping savedKeeping = Keeping.builder()
                .id(3L)
                .storageId(testStorage1.getId())
                .item(testItem2)
                .quantity(15)
                .shelf("C3")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(itemService.getById(2L)).thenReturn(Mono.just(testItem2));
        when(keepingRepository.existsByStorageIdAndItemId(1L, 2L)).thenReturn(false);
        when(keepingRepository.save(newKeeping)).thenReturn(savedKeeping);
        when(storageService.getById(1L)).thenReturn(Mono.just(testStorage1));

        StepVerifier.create(keepingService.create(newKeeping))
                .expectNextMatches(keeping -> {
                    assertNotNull(keeping.getId());
                    assertEquals(testStorage1.getId(), keeping.getStorageId());
                    assertEquals(testItem2.getId(), keeping.getItem().getId());
                    assertEquals(15, keeping.getQuantity());
                    assertEquals("C3", keeping.getShelf());
                    assertNotNull(keeping.getLastUpdated());
                    return true;
                })
                .verifyComplete();

        verify(storageService).getById(1L);
        verify(itemService).getById(2L);
        verify(keepingRepository).existsByStorageIdAndItemId(1L, 2L);
        verify(keepingRepository).save(newKeeping);
    }

    @Test
    void create_ShouldCreateKeeping_WhenShelfIsNull() {
        Keeping newKeeping = Keeping.builder()
                .storageId(testStorage2.getId())
                .item(testItem1)
                .quantity(8)
                .shelf(null)
                .build();

        Keeping savedKeeping = Keeping.builder()
                .id(4L)
                .storageId(testStorage2.getId())
                .item(testItem1)
                .quantity(8)
                .shelf(null)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(storageService.getById(2L)).thenReturn(Mono.just(testStorage2));
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem1));
        when(keepingRepository.existsByStorageIdAndItemId(2L, 1L)).thenReturn(false);
        when(keepingRepository.save(newKeeping)).thenReturn(savedKeeping);

        StepVerifier.create(keepingService.create(newKeeping))
                .expectNextMatches(keeping -> {
                    assertNull(keeping.getShelf());
                    assertEquals(8, keeping.getQuantity());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void create_ShouldThrowDuplicateKeepingException_WhenKeepingExists() {
        Keeping duplicateKeeping = Keeping.builder()
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(3)
                .shelf("Different Shelf")
                .build();

        when(itemService.getById(1L)).thenReturn(Mono.just(testItem1));
        when(keepingRepository.existsByStorageIdAndItemId(1L, 1L)).thenReturn(true);
        when(storageService.getById(1L)).thenReturn(Mono.just(testStorage1));

        StepVerifier.create(keepingService.create(duplicateKeeping))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateKeepingException &&
                                throwable.getMessage().contains("Keeping record already exists for storage ID: 1 and item ID: 1")
                )
                .verify();

        verify(storageService).getById(1L);
        verify(itemService).getById(1L);
        verify(keepingRepository).existsByStorageIdAndItemId(1L, 1L);
        verify(keepingRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnKeeping_WhenKeepingExists() {
        when(keepingRepository.findById(1L)).thenReturn(Optional.of(testKeeping1));

        StepVerifier.create(keepingService.getById(1L))
                .expectNextMatches(keeping -> {
                    assertEquals(testKeeping1.getId(), keeping.getId());
                    assertEquals(testStorage1.getId(), keeping.getStorageId());
                    assertEquals(testItem1.getId(), keeping.getItem().getId());
                    assertEquals(testKeeping1.getQuantity(), keeping.getQuantity());
                    assertEquals(testKeeping1.getShelf(), keeping.getShelf());
                    return true;
                })
                .verifyComplete();

        verify(keepingRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowKeepingNotFoundException_WhenKeepingNotFound() {
        when(keepingRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(keepingService.getById(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof KeepingNotFoundException &&
                                throwable.getMessage().contains("Keeping record not found with ID: 999")
                )
                .verify();

        verify(keepingRepository).findById(999L);
    }

    @Test
    void update_ShouldUpdateKeeping_WhenValidData() {
        Keeping updateData = Keeping.builder()
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(8)
                .shelf("Updated Shelf")
                .build();

        Keeping updatedKeeping = Keeping.builder()
                .id(1L)
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(8)
                .shelf("Updated Shelf")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(keepingRepository.findById(1L)).thenReturn(Optional.of(testKeeping1));
        when(keepingRepository.save(any(Keeping.class))).thenReturn(updatedKeeping);

        StepVerifier.create(keepingService.update(1L, updateData))
                .verifyComplete();

        verify(keepingRepository).findById(1L);
        verify(storageService, never()).getById(anyLong());
        verify(itemService, never()).getById(anyLong());
        verify(keepingRepository, never()).existsByStorageIdAndItemIdAndIdNot(anyLong(), anyLong(), anyLong());
        verify(keepingRepository).save(any(Keeping.class));
    }

    @Test
    void update_ShouldUpdateStorage_WhenStorageChanged() {
        Keeping updateData = Keeping.builder()
                .storageId(testStorage2.getId())
                .item(testItem1)
                .quantity(testKeeping1.getQuantity())
                .shelf(testKeeping1.getShelf())
                .build();

        Keeping updatedKeeping = Keeping.builder()
                .id(1L)
                .storageId(testStorage2.getId())
                .item(testItem1)
                .quantity(testKeeping1.getQuantity())
                .shelf(testKeeping1.getShelf())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(keepingRepository.findById(1L)).thenReturn(Optional.of(testKeeping1));
        when(storageService.getById(2L)).thenReturn(Mono.just(testStorage2));
        when(keepingRepository.save(any(Keeping.class))).thenReturn(updatedKeeping);

        StepVerifier.create(keepingService.update(1L, updateData))
                .verifyComplete();

        verify(keepingRepository).findById(1L);
        verify(storageService).getById(2L);
        verify(itemService, never()).getById(anyLong());
        verify(keepingRepository).save(any(Keeping.class));
    }

    @Test
    void update_ShouldThrowKeepingNotFoundException_WhenKeepingNotFound() {
        Keeping updateData = Keeping.builder()
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(5)
                .shelf("Shelf")
                .build();

        when(keepingRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(keepingService.update(999L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof KeepingNotFoundException &&
                                throwable.getMessage().contains("Keeping record not found with ID: 999")
                )
                .verify();

        verify(keepingRepository).findById(999L);
        verify(storageService, never()).getById(anyLong());
        verify(itemService, never()).getById(anyLong());
        verify(keepingRepository, never()).existsByStorageIdAndItemIdAndIdNot(anyLong(), anyLong(), anyLong());
        verify(keepingRepository, never()).save(any());
    }


    @Test
    void update_ShouldNotCheckDuplicate_WhenOnlyQuantityChanged() {
        Keeping updateData = Keeping.builder()
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(25)
                .shelf(testKeeping1.getShelf())
                .build();

        Keeping updatedKeeping = Keeping.builder()
                .id(1L)
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(25)
                .shelf(testKeeping1.getShelf())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(keepingRepository.findById(1L)).thenReturn(Optional.of(testKeeping1));
        when(keepingRepository.save(any(Keeping.class))).thenReturn(updatedKeeping);

        StepVerifier.create(keepingService.update(1L, updateData))
                .verifyComplete();

        verify(keepingRepository).findById(1L);
        verify(storageService, never()).getById(anyLong());
        verify(itemService, never()).getById(anyLong());
        verify(keepingRepository, never()).existsByStorageIdAndItemIdAndIdNot(anyLong(), anyLong(), anyLong());
        verify(keepingRepository).save(any(Keeping.class));
    }

    @Test
    void delete_ShouldDeleteKeeping_WhenKeepingExists() {
        when(keepingRepository.existsById(1L)).thenReturn(true);
        doNothing().when(keepingRepository).deleteById(1L);

        StepVerifier.create(keepingService.delete(1L))
                .verifyComplete();

        verify(keepingRepository).existsById(1L);
        verify(keepingRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowKeepingNotFoundException_WhenKeepingNotFound() {
        when(keepingRepository.existsById(999L)).thenReturn(false);

        StepVerifier.create(keepingService.delete(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof KeepingNotFoundException &&
                                throwable.getMessage().contains("Keeping record not found with ID: 999")
                )
                .verify();

        verify(keepingRepository).existsById(999L);
        verify(keepingRepository, never()).deleteById(anyLong());
    }

    @Test
    void findKeepingsByFilters_ShouldReturnAllKeepings_WhenNoFilters() {
        List<Keeping> keepings = List.of(testKeeping1, testKeeping2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Keeping> page = new PageImpl<>(keepings, pageable, keepings.size());

        when(keepingRepository.findAll(pageable)).thenReturn(page);

        StepVerifier.create(keepingService.findKeepingsByFilters(null, null, pageable))
                .expectNextCount(2)
                .verifyComplete();

        verify(keepingRepository).findAll(pageable);
    }

    @Test
    void findKeepingsByFilters_ShouldReturnFilteredByStorage_WhenStorageFilterApplied() {
        List<Keeping> keepings = List.of(testKeeping1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Keeping> page = new PageImpl<>(keepings, pageable, keepings.size());

        when(keepingRepository.findByStorageId(1L, pageable)).thenReturn(page);

        StepVerifier.create(keepingService.findKeepingsByFilters(1L, null, pageable))
                .expectNextMatches(keeping -> keeping.getStorageId().equals(1L))
                .verifyComplete();

        verify(keepingRepository).findByStorageId(1L, pageable);
    }

    @Test
    void findKeepingsByFilters_ShouldReturnFilteredByItem_WhenItemFilterApplied() {
        List<Keeping> keepings = List.of(testKeeping2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Keeping> page = new PageImpl<>(keepings, pageable, keepings.size());

        when(keepingRepository.findByItemId(2L, pageable)).thenReturn(page);

        StepVerifier.create(keepingService.findKeepingsByFilters(null, 2L, pageable))
                .expectNextMatches(keeping -> keeping.getItem().getId().equals(2L))
                .verifyComplete();

        verify(keepingRepository).findByItemId(2L, pageable);
    }

    @Test
    void findKeepingsByFilters_ShouldReturnFilteredByStorageAndItem_WhenBothFiltersApplied() {
        List<Keeping> keepings = List.of(testKeeping1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Keeping> page = new PageImpl<>(keepings, pageable, keepings.size());

        when(keepingRepository.findByStorageIdAndItemId(1L, 1L, pageable)).thenReturn(page);

        StepVerifier.create(keepingService.findKeepingsByFilters(1L, 1L, pageable))
                .expectNextMatches(keeping ->
                        keeping.getStorageId().equals(1L) && keeping.getItem().getId().equals(1L)
                )
                .verifyComplete();

        verify(keepingRepository).findByStorageIdAndItemId(1L, 1L, pageable);
    }

    @Test
    void countKeepingsByFilters_ShouldReturnCount_WithStorageFilter() {
        when(keepingRepository.countByStorageId(1L)).thenReturn(2L);

        StepVerifier.create(keepingService.countKeepingsByFilters(1L, null))
                .expectNextMatches(count -> count == 2L)
                .verifyComplete();

        verify(keepingRepository).countByStorageId(1L);
    }

    @Test
    void countKeepingsByFilters_ShouldReturnCount_WithItemFilter() {
        when(keepingRepository.countByItemId(2L)).thenReturn(3L);

        StepVerifier.create(keepingService.countKeepingsByFilters(null, 2L))
                .expectNextMatches(count -> count == 3L)
                .verifyComplete();

        verify(keepingRepository).countByItemId(2L);
    }

    @Test
    void countKeepingsByFilters_ShouldReturnCount_WithBothFilters() {
        when(keepingRepository.countByStorageIdAndItemId(1L, 1L)).thenReturn(1L);

        StepVerifier.create(keepingService.countKeepingsByFilters(1L, 1L))
                .expectNextMatches(count -> count == 1L)
                .verifyComplete();

        verify(keepingRepository).countByStorageIdAndItemId(1L, 1L);
    }

    @Test
    void countKeepingsByFilters_ShouldReturnTotalCount_WhenNoFilters() {
        when(keepingRepository.count()).thenReturn(10L);

        StepVerifier.create(keepingService.countKeepingsByFilters(null, null))
                .expectNextMatches(count -> count == 10L)
                .verifyComplete();

        verify(keepingRepository).count();
    }

    @Test
    void update_ShouldHandleMinimumQuantity() {
        Keeping updateData = Keeping.builder()
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(1)
                .shelf(testKeeping1.getShelf())
                .build();

        Keeping updatedKeeping = Keeping.builder()
                .id(1L)
                .storageId(testStorage1.getId())
                .item(testItem1)
                .quantity(1)
                .shelf(testKeeping1.getShelf())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(keepingRepository.findById(1L)).thenReturn(Optional.of(testKeeping1));
        when(keepingRepository.save(any(Keeping.class))).thenReturn(updatedKeeping);

        StepVerifier.create(keepingService.update(1L, updateData))
                .verifyComplete();
    }
}