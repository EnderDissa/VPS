package com.example.warehouse.service;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.exception.KeepingNotFoundException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.DuplicateKeepingException;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.service.interfaces.KeepingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Transactional
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class KeepingServiceImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private KeepingService keepingService;

    @Autowired
    private KeepingRepository keepingRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Storage testStorage1;
    private Storage testStorage2;
    private Storage nonExistentStorage;
    private Item testItem1;
    private Item testItem2;
    private Item nonExistentItem;
    private Keeping testKeeping1;
    private Keeping testKeeping2;

    @BeforeEach
    void setUp() {
        keepingRepository.deleteAll();
        storageRepository.deleteAll();
        itemRepository.deleteAll();

        testStorage1 = Storage.builder()
                .name("Main Warehouse")
                .address("123 Main St")
                .capacity(1000)
                .createdAt(LocalDateTime.now())
                .build();

        testStorage2 = Storage.builder()
                .name("Secondary Storage")
                .address("456 Oak Ave")
                .capacity(500)
                .createdAt(LocalDateTime.now())
                .build();

        nonExistentStorage = Storage.builder()
                .id(999L)
                .name("Secondary Storage")
                .address("456 Oak Ave")
                .capacity(500)
                .createdAt(LocalDateTime.now())
                .build();

        testStorage1 = storageRepository.save(testStorage1);
        testStorage2 = storageRepository.save(testStorage2);

        testItem1 = Item.builder()
                .name("Laptop Dell XPS")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .description("High-performance laptop")
                .createdAt(LocalDateTime.now())
                .build();

        testItem2 = Item.builder()
                .name("Office Chair")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.NEW)
                .serialNumber("SN789012")
                .description("Ergonomic office chair")
                .createdAt(LocalDateTime.now())
                .build();

        nonExistentItem = Item.builder()
                .id(999L)
                .name("Office Chair")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.NEW)
                .serialNumber("SN789012")
                .description("Ergonomic office chair")
                .createdAt(LocalDateTime.now())
                .build();

        testItem1 = itemRepository.save(testItem1);
        testItem2 = itemRepository.save(testItem2);

        // Создаем тестовые записи хранения
        testKeeping1 = Keeping.builder()
                .storage(testStorage1)
                .item(testItem1)
                .quantity(5)
                .shelf("A1")
                .lastUpdated(LocalDateTime.now().minusDays(2))
                .build();

        testKeeping2 = Keeping.builder()
                .storage(testStorage2)
                .item(testItem2)
                .quantity(10)
                .shelf("B2")
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();

        testKeeping1 = keepingRepository.save(testKeeping1);
        testKeeping2 = keepingRepository.save(testKeeping2);
    }

    // Тесты для метода create
    @Test
    void create_ShouldCreateKeeping_WhenValidData() {
        Keeping newKeeping = new Keeping(
                null,
                testStorage1,
                testItem2,
                15,
                "C3",
                null
        );

        Keeping result = keepingService.create(newKeeping);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testStorage1.getId(), result.getStorage().getId());
        assertEquals(testItem2.getId(), result.getItem().getId());
        assertEquals(15, result.getQuantity());
        assertEquals("C3", result.getShelf());

        Keeping savedKeeping = keepingRepository.findById(result.getId()).orElseThrow();
        assertEquals(testStorage1.getId(), savedKeeping.getStorage().getId());
        assertEquals(testItem2.getId(), savedKeeping.getItem().getId());
        assertEquals(15, savedKeeping.getQuantity());
        assertEquals("C3", savedKeeping.getShelf());
    }

    @Test
    void create_ShouldCreateKeeping_WhenShelfIsNull() {
        Keeping newKeeping = new Keeping(
                null,
                testStorage2,
                testItem1,
                8,
                null,
                null
        );

        Keeping result = keepingService.create(newKeeping);

        assertNotNull(result);
        assertNull(result.getShelf());
        assertEquals(8, result.getQuantity());
    }

    @Test
    void create_ShouldUseDefaultQuantity_WhenQuantityIsNull() {
        Keeping newKeeping = Keeping.builder()
                .item(testItem2)
                .storage(testStorage1)
                .shelf("D4")
                .build();
        Keeping result = keepingService.create(newKeeping);

        assertNotNull(result);
        assertEquals(1, result.getQuantity());
    }

    @Test
    void create_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
        long nonExistentStorageId = 999L;
        Keeping newKeeping = new Keeping(
                null,
                nonExistentStorage,
                testItem1,
                5,
                "A1",
                null
        );

        StorageNotFoundException exception = assertThrows(
                StorageNotFoundException.class,
                () -> keepingService.create(newKeeping)
        );

        assertTrue(exception.getMessage().contains("Storage not found with ID: " + nonExistentStorageId));
    }

    @Test
    void create_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        long nonExistentItemId = 999L;
        Keeping newKeeping = new Keeping(
                null,
                testStorage1,
                nonExistentItem,
                5,
                "A1",
                null
        );

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> keepingService.create(newKeeping)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentItemId));
    }

    @Test
    void create_ShouldThrowDuplicateKeepingException_WhenKeepingExists() {
        Keeping duplicateKeeping = new Keeping(
                null,
                testStorage1,
                testItem1,
                3,
                "Different Shelf",
                null
        );

        DuplicateKeepingException exception = assertThrows(
                DuplicateKeepingException.class,
                () -> keepingService.create(duplicateKeeping)
        );

        assertTrue(exception.getMessage().contains("Keeping record already exists for storage ID: " +
                testStorage1.getId() + " and item ID: " + testItem1.getId()));
    }

    // Тесты для метода getById
    @Test
    void getById_ShouldReturnKeeping_WhenKeepingExists() {
        Keeping result = keepingService.getById(testKeeping1.getId());

        assertNotNull(result);
        assertEquals(testKeeping1.getId(), result.getId());
        assertEquals(testStorage1.getId(), result.getStorage().getId());
        assertEquals(testItem1.getId(), result.getItem().getId());
        assertEquals(testKeeping1.getQuantity(), result.getQuantity());
        assertEquals(testKeeping1.getShelf(), result.getShelf());
    }

    @Test
    void getById_ShouldThrowKeepingNotFoundException_WhenKeepingNotFound() {
        Long nonExistentId = 999L;

        KeepingNotFoundException exception = assertThrows(
                KeepingNotFoundException.class,
                () -> keepingService.getById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Keeping record not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldUpdateKeeping_WhenValidData() {
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testKeeping1.getStorage(),
                testKeeping1.getItem(),
                8,
                "Updated Shelf",
                null
        );

        keepingService.update(testKeeping1.getId(), update);

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(8, updatedKeeping.getQuantity());
        assertEquals("Updated Shelf", updatedKeeping.getShelf());
        assertEquals(testStorage1.getId(), updatedKeeping.getStorage().getId());
        assertEquals(testItem1.getId(), updatedKeeping.getItem().getId());
    }

    @Test
    void update_ShouldUpdateStorage_WhenStorageChanged() {
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testStorage2,
                testKeeping1.getItem(),
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        keepingService.update(testKeeping1.getId(), update);

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(testStorage2.getId(), updatedKeeping.getStorage().getId());
        assertEquals(testItem1.getId(), updatedKeeping.getItem().getId());
    }

    @Test
    void update_ShouldUpdateItem_WhenItemChanged() {
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testKeeping1.getStorage(),
                testItem2,
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        keepingService.update(testKeeping1.getId(), update);

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(testStorage1.getId(), updatedKeeping.getStorage().getId());
        assertEquals(testItem2.getId(), updatedKeeping.getItem().getId());
    }

    @Test
    void update_ShouldThrowKeepingNotFoundException_WhenKeepingNotFound() {
        Long nonExistentId = 999L;
        Keeping update = new Keeping(
                nonExistentId,
                testStorage1,
                testItem1,
                5,
                "Shelf",
                null
        );

        KeepingNotFoundException exception = assertThrows(
                KeepingNotFoundException.class,
                () -> keepingService.update(nonExistentId, update)
        );

        assertTrue(exception.getMessage().contains("Keeping record not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldThrowStorageNotFoundException_WhenNewStorageNotFound() {
        Long nonExistentStorageId = 999L;
        Keeping update = new Keeping(
                testKeeping1.getId(),
                nonExistentStorage,
                testKeeping1.getItem(),
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        StorageNotFoundException exception = assertThrows(
                StorageNotFoundException.class,
                () -> keepingService.update(testKeeping1.getId(), update)
        );

        assertTrue(exception.getMessage().contains("Storage not found with ID: " + nonExistentStorageId));
    }

    @Test
    void update_ShouldThrowItemNotFoundException_WhenNewItemNotFound() {
        long nonExistentItemId = 999L;
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testKeeping1.getStorage(),
                nonExistentItem,
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> keepingService.update(testKeeping1.getId(), update)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentItemId));
    }

    @Test
    void update_ShouldThrowDuplicateKeepingException_WhenItemChangedToExistingCombination() {
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testStorage2,
                testItem2,
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        DuplicateKeepingException exception = assertThrows(
                DuplicateKeepingException.class,
                () -> keepingService.update(testKeeping1.getId(), update)
        );

        assertTrue(exception.getMessage().contains("Keeping record already exists for storage ID: " +
                testStorage2.getId() + " and item ID: " + testItem2.getId()));
    }

    @Test
    void update_ShouldNotCheckDuplicate_WhenOnlyQuantityChanged() {
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testKeeping1.getStorage(),
                testKeeping1.getItem(),
                25,
                testKeeping1.getShelf(),
                null
        );

        assertDoesNotThrow(() -> keepingService.update(testKeeping1.getId(), update));

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(25, updatedKeeping.getQuantity());
    }

    @Test
    void update_ShouldNotCheckDuplicate_WhenOnlyShelfChanged() {
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testKeeping1.getStorage(),
                testKeeping1.getItem(),
                testKeeping1.getQuantity(),
                "New Shelf Location",
                null
        );

        assertDoesNotThrow(() -> keepingService.update(testKeeping1.getId(), update));

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals("New Shelf Location", updatedKeeping.getShelf());
    }

    @Test
    void delete_ShouldDeleteKeeping_WhenKeepingExists() {
        Long keepingId = testKeeping1.getId();

        keepingService.delete(keepingId);

        assertFalse(keepingRepository.existsById(keepingId));
    }

    @Test
    void delete_ShouldThrowKeepingNotFoundException_WhenKeepingNotFound() {
        Long nonExistentId = 999L;

        KeepingNotFoundException exception = assertThrows(
                KeepingNotFoundException.class,
                () -> keepingService.delete(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Keeping record not found with ID: " + nonExistentId));
    }

    @Test
    void findPage_ShouldReturnAllKeepings_WhenNoFilters() {
        Page<Keeping> result = keepingService.findPage(0, 10, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findPage_ShouldReturnFilteredByStorage_WhenStorageFilterApplied() {
        Page<Keeping> result = keepingService.findPage(0, 10, testStorage1.getId(), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        Keeping keepingDTO = result.getContent().get(0);
        assertEquals(testStorage1.getId(), keepingDTO.getStorage().getId());
        assertEquals(testKeeping1.getId(), keepingDTO.getId());
    }

    @Test
    void findPage_ShouldReturnFilteredByItem_WhenItemFilterApplied() {
        Page<Keeping> result = keepingService.findPage(0, 10, null, testItem2.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        Keeping keepingDTO = result.getContent().get(0);
        assertEquals(testItem2.getId(), keepingDTO.getItem().getId());
        assertEquals(testKeeping2.getId(), keepingDTO.getId());
    }

    @Test
    void findPage_ShouldReturnFilteredByStorageAndItem_WhenBothFiltersApplied() {
        Page<Keeping> result = keepingService.findPage(0, 10, testStorage1.getId(), testItem1.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        Keeping keepingDTO = result.getContent().get(0);
        assertEquals(testStorage1.getId(), keepingDTO.getStorage().getId());
        assertEquals(testItem1.getId(), keepingDTO.getItem().getId());
        assertEquals(testKeeping1.getId(), keepingDTO.getId());
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatchingFilters() {
        Page<Keeping> result = keepingService.findPage(0, 10, 999L, 999L);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
        Page<Keeping> result = keepingService.findPage(0, 1, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void findPage_ShouldReturnSecondPage_WhenPageOneRequested() {
        Page<Keeping> result = keepingService.findPage(1, 1, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
    }

    @Test
    void findPage_ShouldReturnSortedByLastUpdatedDesc() {
        Keeping newKeeping = Keeping.builder()
                .storage(testStorage1)
                .item(testItem2)
                .quantity(3)
                .shelf("Z9")
                .lastUpdated(LocalDateTime.now())
                .build();
        keepingRepository.save(newKeeping);

        Page<Keeping> result = keepingService.findPage(0, 10, null, null);

        List<Keeping> content = result.getContent();

        assertEquals(newKeeping.getId(), content.get(0).getId());
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
        Page<Keeping> result = keepingService.findPage(10, 10, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void update_ShouldHandleMinimumQuantity() {
        Keeping update = new Keeping(
                testKeeping1.getId(),
                testKeeping1.getStorage(),
                testKeeping1.getItem(),
                1,
                testKeeping1.getShelf(),
                null
        );

        assertDoesNotThrow(() -> keepingService.update(testKeeping1.getId(), update));

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(1, updatedKeeping.getQuantity());
    }
}