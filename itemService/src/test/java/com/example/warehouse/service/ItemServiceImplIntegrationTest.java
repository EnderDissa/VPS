package com.example.warehouse.service;

import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.DuplicateSerialNumberException;
import com.example.warehouse.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item testItem1;
    private Item testItem2;
    private Item testItem3;

    @BeforeEach
    void setUp() {
        testItem1 = Item.builder()
                .id(1L)
                .name("Laptop Dell XPS")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .description("High-performance laptop")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        testItem2 = Item.builder()
                .id(2L)
                .name("Office Chair")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.NEW)
                .serialNumber("SN789012")
                .description("Ergonomic office chair")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        testItem3 = Item.builder()
                .id(3L)
                .name("Broken Monitor")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.UNDER_REPAIR)
                .serialNumber("SN345678")
                .description("Needs repair")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void create_ShouldCreateItem_WhenValidData() {
        Item newItem = Item.builder()
                .name("New Tablet")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.NEW)
                .serialNumber("SN999999")
                .description("Brand new tablet")
                .build();

        Item savedItem = Item.builder()
                .id(4L)
                .name("New Tablet")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.NEW)
                .serialNumber("SN999999")
                .description("Brand new tablet")
                .createdAt(LocalDateTime.now())
                .build();

        when(itemRepository.existsBySerialNumber("SN999999")).thenReturn(false);
        when(itemRepository.save(newItem)).thenReturn(savedItem);

        StepVerifier.create(itemService.create(newItem))
                .expectNextMatches(item -> {
                    assertNotNull(item.getId());
                    assertEquals("New Tablet", item.getName());
                    assertEquals(ItemType.ELECTRONICS, item.getType());
                    assertEquals(ItemCondition.NEW, item.getCondition());
                    assertEquals("SN999999", item.getSerialNumber());
                    assertEquals("Brand new tablet", item.getDescription());
                    return true;
                })
                .verifyComplete();

        verify(itemRepository).existsBySerialNumber("SN999999");
        verify(itemRepository).save(newItem);
    }

    @Test
    void create_ShouldCreateItem_WhenSerialNumberIsNull() {
        Item newItem = Item.builder()
                .name("Item Without Serial")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.GOOD)
                .serialNumber(null)
                .description("No serial number")
                .build();

        Item savedItem = Item.builder()
                .id(5L)
                .name("Item Without Serial")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.GOOD)
                .serialNumber(null)
                .description("No serial number")
                .createdAt(LocalDateTime.now())
                .build();

        when(itemRepository.save(newItem)).thenReturn(savedItem);

        StepVerifier.create(itemService.create(newItem))
                .expectNextMatches(item -> {
                    assertNull(item.getSerialNumber());
                    assertEquals("Item Without Serial", item.getName());
                    return true;
                })
                .verifyComplete();

        verify(itemRepository, never()).existsBySerialNumber(anyString());
        verify(itemRepository).save(newItem);
    }

    @Test
    void create_ShouldCreateItem_WhenSerialNumberIsEmpty() {
        Item newItem = Item.builder()
                .name("Item With Empty Serial")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.GOOD)
                .serialNumber("")
                .description("Empty serial number")
                .build();

        Item savedItem = Item.builder()
                .id(6L)
                .name("Item With Empty Serial")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.GOOD)
                .serialNumber("")
                .description("Empty serial number")
                .createdAt(LocalDateTime.now())
                .build();

        when(itemRepository.save(newItem)).thenReturn(savedItem);

        StepVerifier.create(itemService.create(newItem))
                .expectNextMatches(item -> {
                    assertEquals("", item.getSerialNumber());
                    return true;
                })
                .verifyComplete();

        verify(itemRepository, never()).existsBySerialNumber(anyString());
        verify(itemRepository).save(newItem);
    }

    @Test
    void create_ShouldThrowDuplicateSerialNumberException_WhenSerialNumberExists() {
        Item newItem = Item.builder()
                .name("Another Laptop")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .description("Duplicate serial")
                .build();

        when(itemRepository.existsBySerialNumber("SN123456")).thenReturn(true);

        StepVerifier.create(itemService.create(newItem))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateSerialNumberException &&
                                throwable.getMessage().contains("Item with serial number 'SN123456' already exists")
                )
                .verify();

        verify(itemRepository).existsBySerialNumber("SN123456");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnItem_WhenItemExists() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem1));

        StepVerifier.create(itemService.getById(1L))
                .expectNextMatches(item -> {
                    assertEquals(testItem1.getId(), item.getId());
                    assertEquals(testItem1.getName(), item.getName());
                    assertEquals(testItem1.getType(), item.getType());
                    assertEquals(testItem1.getCondition(), item.getCondition());
                    assertEquals(testItem1.getSerialNumber(), item.getSerialNumber());
                    assertEquals(testItem1.getDescription(), item.getDescription());
                    return true;
                })
                .verifyComplete();

        verify(itemRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(itemService.getById(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                                throwable.getMessage().contains("Item not found with ID: 999")
                )
                .verify();

        verify(itemRepository).findById(999L);
    }

    @Test
    void update_ShouldUpdateItem_WhenValidData() {
        Item updateData = Item.builder()
                .name("Updated Laptop Name")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.EXCELLENT)
                .serialNumber("SN123456")
                .description("Updated description")
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("Updated Laptop Name")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.EXCELLENT)
                .serialNumber("SN123456")
                .description("Updated description")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        StepVerifier.create(itemService.update(1L, updateData))
                .verifyComplete();

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).existsBySerialNumber(anyString());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_ShouldUpdateItem_WhenSerialNumberChangedToUnique() {
        Item updateData = Item.builder()
                .name(testItem1.getName())
                .type(testItem1.getType())
                .condition(testItem1.getCondition())
                .serialNumber("SN_NEW_UNIQUE")
                .description(testItem1.getDescription())
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name(testItem1.getName())
                .type(testItem1.getType())
                .condition(testItem1.getCondition())
                .serialNumber("SN_NEW_UNIQUE")
                .description(testItem1.getDescription())
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.existsBySerialNumber("SN_NEW_UNIQUE")).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        StepVerifier.create(itemService.update(1L, updateData))
                .verifyComplete();

        verify(itemRepository).findById(1L);
        verify(itemRepository).existsBySerialNumber("SN_NEW_UNIQUE");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_ShouldUpdateItem_WhenSerialNumberSetToNull() {
        Item updateData = Item.builder()
                .name(testItem1.getName())
                .type(testItem1.getType())
                .condition(testItem1.getCondition())
                .serialNumber(null)
                .description(testItem1.getDescription())
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name(testItem1.getName())
                .type(testItem1.getType())
                .condition(testItem1.getCondition())
                .serialNumber(null)
                .description(testItem1.getDescription())
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        StepVerifier.create(itemService.update(1L, updateData))
                .verifyComplete();

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).existsBySerialNumber(anyString());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        Item updateData = Item.builder()
                .name("Non-existent Item")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN000000")
                .description("Description")
                .build();

        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(itemService.update(999L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                                throwable.getMessage().contains("Item not found with ID: 999")
                )
                .verify();

        verify(itemRepository).findById(999L);
        verify(itemRepository, never()).existsBySerialNumber(anyString());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowDuplicateSerialNumberException_WhenSerialNumberExistsOnOtherItem() {
        Item updateData = Item.builder()
                .name(testItem1.getName())
                .type(testItem1.getType())
                .condition(testItem1.getCondition())
                .serialNumber("SN789012")
                .description(testItem1.getDescription())
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.existsBySerialNumber("SN789012")).thenReturn(true);

        StepVerifier.create(itemService.update(1L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateSerialNumberException &&
                                throwable.getMessage().contains("Item with serial number 'SN789012' already exists")
                )
                .verify();

        verify(itemRepository).findById(1L);
        verify(itemRepository).existsBySerialNumber("SN789012");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_ShouldNotCheckSerialNumber_WhenSerialNumberNotChanged() {
        Item updateData = Item.builder()
                .name("Updated Name")
                .type(testItem1.getType())
                .condition(testItem1.getCondition())
                .serialNumber(testItem1.getSerialNumber())
                .description("Updated description")
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("Updated Name")
                .type(testItem1.getType())
                .condition(testItem1.getCondition())
                .serialNumber(testItem1.getSerialNumber())
                .description("Updated description")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem1));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        StepVerifier.create(itemService.update(1L, updateData))
                .verifyComplete();

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).existsBySerialNumber(anyString());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void delete_ShouldDeleteItem_WhenItemExists() {
        when(itemRepository.existsById(1L)).thenReturn(true);
        doNothing().when(itemRepository).deleteById(1L);

        StepVerifier.create(itemService.delete(1L))
                .verifyComplete();

        verify(itemRepository).existsById(1L);
        verify(itemRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        when(itemRepository.existsById(999L)).thenReturn(false);

        StepVerifier.create(itemService.delete(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                                throwable.getMessage().contains("Item not found with ID: 999")
                )
                .verify();

        verify(itemRepository).existsById(999L);
        verify(itemRepository, never()).deleteById(anyLong());
    }

    @Test
    void findItemsByFilters_ShouldReturnAllItems_WhenNoFilters() {
        List<Item> items = List.of(testItem3, testItem2, testItem1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findAll(pageable)).thenReturn(page);

        StepVerifier.create(itemService.findItemsByFilters(null, null, pageable))
                .expectNextMatches(item -> item.getId().equals(3L))
                .expectNextMatches(item -> item.getId().equals(2L))
                .expectNextMatches(item -> item.getId().equals(1L))
                .verifyComplete();

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void findItemsByFilters_ShouldReturnFilteredByType_WhenTypeFilterApplied() {
        List<Item> items = List.of(testItem3, testItem1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByType(ItemType.ELECTRONICS, pageable)).thenReturn(page);

        StepVerifier.create(itemService.findItemsByFilters(ItemType.ELECTRONICS, null, pageable))
                .expectNextMatches(item -> item.getType() == ItemType.ELECTRONICS)
                .expectNextMatches(item -> item.getType() == ItemType.ELECTRONICS)
                .verifyComplete();

        verify(itemRepository).findByType(ItemType.ELECTRONICS, pageable);
    }

    @Test
    void findItemsByFilters_ShouldReturnFilteredByCondition_WhenConditionFilterApplied() {
        List<Item> items = List.of(testItem2);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByCondition(ItemCondition.NEW, pageable)).thenReturn(page);

        StepVerifier.create(itemService.findItemsByFilters(null, ItemCondition.NEW, pageable))
                .expectNextMatches(item -> item.getCondition() == ItemCondition.NEW)
                .verifyComplete();

        verify(itemRepository).findByCondition(ItemCondition.NEW, pageable);
    }

    @Test
    void findItemsByFilters_ShouldReturnFilteredByTypeAndCondition_WhenBothFiltersApplied() {
        List<Item> items = List.of(testItem1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByTypeAndCondition(ItemType.ELECTRONICS, ItemCondition.GOOD, pageable))
                .thenReturn(page);

        StepVerifier.create(itemService.findItemsByFilters(ItemType.ELECTRONICS, ItemCondition.GOOD, pageable))
                .expectNextMatches(item -> {
                    assertEquals(ItemType.ELECTRONICS, item.getType());
                    assertEquals(ItemCondition.GOOD, item.getCondition());
                    return true;
                })
                .verifyComplete();

        verify(itemRepository).findByTypeAndCondition(ItemType.ELECTRONICS, ItemCondition.GOOD, pageable);
    }

    @Test
    void countItemsByFilters_ShouldReturnCount_WithTypeFilter() {
        List<Item> items = List.of(testItem3, testItem1);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByType(ItemType.ELECTRONICS, pageable)).thenReturn(page);

        StepVerifier.create(itemService.countItemsByFilters(ItemType.ELECTRONICS, null))
                .expectNextMatches(count -> count == 2L)
                .verifyComplete();

        verify(itemRepository).findByType(ItemType.ELECTRONICS, pageable);
    }

    @Test
    void countItemsByFilters_ShouldReturnCount_WithConditionFilter() {
        List<Item> items = List.of(testItem2);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByCondition(ItemCondition.NEW, pageable)).thenReturn(page);

        StepVerifier.create(itemService.countItemsByFilters(null, ItemCondition.NEW))
                .expectNextMatches(count -> count == 1L)
                .verifyComplete();

        verify(itemRepository).findByCondition(ItemCondition.NEW, pageable);
    }

    @Test
    void countItemsByFilters_ShouldReturnCount_WithBothFilters() {
        List<Item> items = List.of(testItem1);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByTypeAndCondition(ItemType.ELECTRONICS, ItemCondition.GOOD, pageable))
                .thenReturn(page);

        StepVerifier.create(itemService.countItemsByFilters(ItemType.ELECTRONICS, ItemCondition.GOOD))
                .expectNextMatches(count -> count == 1L)
                .verifyComplete();

        verify(itemRepository).findByTypeAndCondition(ItemType.ELECTRONICS, ItemCondition.GOOD, pageable);
    }

    @Test
    void countItemsByFilters_ShouldReturnTotalCount_WhenNoFilters() {
        when(itemRepository.count()).thenReturn(3L);

        StepVerifier.create(itemService.countItemsByFilters(null, null))
                .expectNextMatches(count -> count == 3L)
                .verifyComplete();

        verify(itemRepository).count();
    }

    @Test
    void findAvailable_ShouldReturnAllItems_WhenNoFiltersAndNoCursor() {
        List<Item> items = List.of(testItem1, testItem2, testItem3);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findAll(pageable)).thenReturn(page);

        StepVerifier.create(itemService.findAvailable(
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(10),
                        null, null, null, null, 10
                ))
                .expectNextCount(3)
                .verifyComplete();

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void findAvailable_ShouldReturnLimitedItems_WhenLimitSpecified() {
        List<Item> items = List.of(testItem1, testItem2);
        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findAll(pageable)).thenReturn(page);

        StepVerifier.create(itemService.findAvailable(
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(10),
                        null, null, null, null, 2
                ))
                .expectNextCount(2)
                .verifyComplete();

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void findAvailable_ShouldReturnItemsWithCursor_WhenCursorSpecified() {
        Long cursorId = 1L;
        List<Item> items = List.of(testItem2, testItem3);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByIdGreaterThan(cursorId, pageable)).thenReturn(page);

        StepVerifier.create(itemService.findAvailable(
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(10),
                        null, null, null, cursorId, 10
                ))
                .expectNextCount(2)
                .verifyComplete();

        verify(itemRepository).findByIdGreaterThan(cursorId, pageable);
    }

    @Test
    void findAvailable_ShouldReturnFilteredByType_WhenTypeFilterApplied() {
        List<Item> items = List.of(testItem1, testItem3);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByType(ItemType.ELECTRONICS, pageable)).thenReturn(page);

        StepVerifier.create(itemService.findAvailable(
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(10),
                        null, ItemType.ELECTRONICS, null, null, 10
                ))
                .expectNextMatches(item -> item.getType() == ItemType.ELECTRONICS)
                .expectNextMatches(item -> item.getType() == ItemType.ELECTRONICS)
                .verifyComplete();

        verify(itemRepository).findByType(ItemType.ELECTRONICS, pageable);
    }

    @Test
    void findAvailable_ShouldReturnFilteredByTypeAndCondition_WhenBothFiltersApplied() {
        List<Item> items = List.of(testItem1);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByTypeAndCondition(ItemType.ELECTRONICS, ItemCondition.GOOD, pageable))
                .thenReturn(page);

        StepVerifier.create(itemService.findAvailable(
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(10),
                        null, ItemType.ELECTRONICS, ItemCondition.GOOD, null, 10
                ))
                .expectNextMatches(item -> {
                    assertEquals(ItemType.ELECTRONICS, item.getType());
                    assertEquals(ItemCondition.GOOD, item.getCondition());
                    return true;
                })
                .verifyComplete();

        verify(itemRepository).findByTypeAndCondition(ItemType.ELECTRONICS, ItemCondition.GOOD, pageable);
    }

    @Test
    void findAvailable_ShouldReturnItemsWithCursorAndTypeFilter() {
        Long cursorId = 1L;
        List<Item> items = List.of(testItem3);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findByIdGreaterThanAndType(cursorId, ItemType.ELECTRONICS, pageable))
                .thenReturn(page);

        StepVerifier.create(itemService.findAvailable(
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(10),
                        null, ItemType.ELECTRONICS, null, cursorId, 10
                ))
                .expectNextMatches(item ->
                        item.getId() > cursorId && item.getType() == ItemType.ELECTRONICS
                )
                .verifyComplete();

        verify(itemRepository).findByIdGreaterThanAndType(cursorId, ItemType.ELECTRONICS, pageable);
    }

    @Test
    void findAvailable_ShouldReturnSortedByIdAsc() {
        List<Item> items = List.of(testItem1, testItem2, testItem3);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> page = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findAll(pageable)).thenReturn(page);

        StepVerifier.create(itemService.findAvailable(
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(10),
                        null, null, null, null, 10
                ))
                .expectNextMatches(item -> item.getId().equals(1L))
                .expectNextMatches(item -> item.getId().equals(2L))
                .expectNextMatches(item -> item.getId().equals(3L))
                .verifyComplete();

        verify(itemRepository).findAll(pageable);
    }
}