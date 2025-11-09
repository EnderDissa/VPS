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
    private KeepingServiceImpl keepingService;

    @Autowired
    private KeepingRepository keepingRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Storage testStorage1;
    private Storage testStorage2;
    private Item testItem1;
    private Item testItem2;
    private Keeping testKeeping1;
    private Keeping testKeeping2;

    @BeforeEach
    void setUp() {
        keepingRepository.deleteAll();
        storageRepository.deleteAll();
        itemRepository.deleteAll();

        // Создаем тестовые склады
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

        testStorage1 = storageRepository.save(testStorage1);
        testStorage2 = storageRepository.save(testStorage2);

        // Создаем тестовые предметы
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
        KeepingDTO newKeepingDTO = new KeepingDTO(
                null,
                testStorage1.getId(),
                testItem2.getId(), // Новый предмет для этого склада
                15,
                "C3",
                null
        );

        KeepingDTO result = keepingService.create(newKeepingDTO);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(testStorage1.getId(), result.storageId());
        assertEquals(testItem2.getId(), result.itemId());
        assertEquals(15, result.quantity());
        assertEquals("C3", result.shelf());

        // Проверяем, что объект действительно сохранен в БД
        Keeping savedKeeping = keepingRepository.findById(result.id()).orElseThrow();
        assertEquals(testStorage1.getId(), savedKeeping.getStorage().getId());
        assertEquals(testItem2.getId(), savedKeeping.getItem().getId());
        assertEquals(15, savedKeeping.getQuantity());
        assertEquals("C3", savedKeeping.getShelf());
    }

    @Test
    void create_ShouldCreateKeeping_WhenShelfIsNull() {
        KeepingDTO newKeepingDTO = new KeepingDTO(
                null,
                testStorage2.getId(),
                testItem1.getId(),
                8,
                null, // null shelf
                null
        );

        KeepingDTO result = keepingService.create(newKeepingDTO);

        assertNotNull(result);
        assertNull(result.shelf());
        assertEquals(8, result.quantity());
    }

    @Test
    void create_ShouldUseDefaultQuantity_WhenQuantityIsNull() {
        KeepingDTO newKeepingDTO = new KeepingDTO(
                null,
                testStorage1.getId(),
                testItem2.getId(),
                null, // null quantity - должно использоваться значение по умолчанию 1
                "D4",
                null
        );

        KeepingDTO result = keepingService.create(newKeepingDTO);

        assertNotNull(result);
        assertEquals(1, result.quantity()); // Проверяем значение по умолчанию
    }

    @Test
    void create_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
        Long nonExistentStorageId = 999L;
        KeepingDTO newKeepingDTO = new KeepingDTO(
                null,
                nonExistentStorageId,
                testItem1.getId(),
                5,
                "A1",
                null
        );

        StorageNotFoundException exception = assertThrows(
                StorageNotFoundException.class,
                () -> keepingService.create(newKeepingDTO)
        );

        assertTrue(exception.getMessage().contains("Storage not found with ID: " + nonExistentStorageId));
    }

    @Test
    void create_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        Long nonExistentItemId = 999L;
        KeepingDTO newKeepingDTO = new KeepingDTO(
                null,
                testStorage1.getId(),
                nonExistentItemId,
                5,
                "A1",
                null
        );

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> keepingService.create(newKeepingDTO)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentItemId));
    }

    @Test
    void create_ShouldThrowDuplicateKeepingException_WhenKeepingExists() {
        // Пытаемся создать запись с тем же складом и предметом, что и testKeeping1
        KeepingDTO duplicateKeepingDTO = new KeepingDTO(
                null,
                testStorage1.getId(),
                testItem1.getId(), // Такая же комбинация как у testKeeping1
                3,
                "Different Shelf",
                null
        );

        DuplicateKeepingException exception = assertThrows(
                DuplicateKeepingException.class,
                () -> keepingService.create(duplicateKeepingDTO)
        );

        assertTrue(exception.getMessage().contains("Keeping record already exists for storage ID: " +
                testStorage1.getId() + " and item ID: " + testItem1.getId()));
    }

    // Тесты для метода getById
    @Test
    void getById_ShouldReturnKeeping_WhenKeepingExists() {
        KeepingDTO result = keepingService.getById(testKeeping1.getId());

        assertNotNull(result);
        assertEquals(testKeeping1.getId(), result.id());
        assertEquals(testStorage1.getId(), result.storageId());
        assertEquals(testItem1.getId(), result.itemId());
        assertEquals(testKeeping1.getQuantity(), result.quantity());
        assertEquals(testKeeping1.getShelf(), result.shelf());
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

    // Тесты для метода update
    @Test
    void update_ShouldUpdateKeeping_WhenValidData() {
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testKeeping1.getStorage().getId(), // Тот же склад
                testKeeping1.getItem().getId(),    // Тот же предмет
                8,                                 // Новое количество
                "Updated Shelf",                   // Новая полка
                null
        );

        keepingService.update(testKeeping1.getId(), updateDTO);

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(8, updatedKeeping.getQuantity());
        assertEquals("Updated Shelf", updatedKeeping.getShelf());
        assertEquals(testStorage1.getId(), updatedKeeping.getStorage().getId());
        assertEquals(testItem1.getId(), updatedKeeping.getItem().getId());
    }

    @Test
    void update_ShouldUpdateStorage_WhenStorageChanged() {
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testStorage2.getId(), // Новый склад
                testKeeping1.getItem().getId(),
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        keepingService.update(testKeeping1.getId(), updateDTO);

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(testStorage2.getId(), updatedKeeping.getStorage().getId());
        assertEquals(testItem1.getId(), updatedKeeping.getItem().getId());
    }

    @Test
    void update_ShouldUpdateItem_WhenItemChanged() {
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testKeeping1.getStorage().getId(),
                testItem2.getId(), // Новый предмет
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        keepingService.update(testKeeping1.getId(), updateDTO);

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(testStorage1.getId(), updatedKeeping.getStorage().getId());
        assertEquals(testItem2.getId(), updatedKeeping.getItem().getId());
    }

    @Test
    void update_ShouldThrowKeepingNotFoundException_WhenKeepingNotFound() {
        Long nonExistentId = 999L;
        KeepingDTO updateDTO = new KeepingDTO(
                nonExistentId,
                testStorage1.getId(),
                testItem1.getId(),
                5,
                "Shelf",
                null
        );

        KeepingNotFoundException exception = assertThrows(
                KeepingNotFoundException.class,
                () -> keepingService.update(nonExistentId, updateDTO)
        );

        assertTrue(exception.getMessage().contains("Keeping record not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldThrowStorageNotFoundException_WhenNewStorageNotFound() {
        Long nonExistentStorageId = 999L;
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                nonExistentStorageId, // Несуществующий склад
                testKeeping1.getItem().getId(),
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        StorageNotFoundException exception = assertThrows(
                StorageNotFoundException.class,
                () -> keepingService.update(testKeeping1.getId(), updateDTO)
        );

        assertTrue(exception.getMessage().contains("Storage not found with ID: " + nonExistentStorageId));
    }

    @Test
    void update_ShouldThrowItemNotFoundException_WhenNewItemNotFound() {
        Long nonExistentItemId = 999L;
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testKeeping1.getStorage().getId(),
                nonExistentItemId, // Несуществующий предмет
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> keepingService.update(testKeeping1.getId(), updateDTO)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentItemId));
    }

    @Test
    void update_ShouldThrowDuplicateKeepingException_WhenItemChangedToExistingCombination() {
        // Меняем предмет у testKeeping1 на предмет testKeeping2, но оставляем склад testKeeping1
        // Если такая комбинация уже существует (testKeeping2), должно выброситься исключение
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testStorage2.getId(), // Склад testKeeping2
                testItem2.getId(),    // Предмет testKeeping2 - дублирующая комбинация
                testKeeping1.getQuantity(),
                testKeeping1.getShelf(),
                null
        );

        DuplicateKeepingException exception = assertThrows(
                DuplicateKeepingException.class,
                () -> keepingService.update(testKeeping1.getId(), updateDTO)
        );

        assertTrue(exception.getMessage().contains("Keeping record already exists for storage ID: " +
                testStorage2.getId() + " and item ID: " + testItem2.getId()));
    }

    @Test
    void update_ShouldNotCheckDuplicate_WhenOnlyQuantityChanged() {
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testKeeping1.getStorage().getId(), // Тот же склад
                testKeeping1.getItem().getId(),    // Тот же предмет
                25,                                // Только количество меняется
                testKeeping1.getShelf(),
                null
        );

        // Не должно быть исключения дублирования, так как склад и предмет не меняются
        assertDoesNotThrow(() -> keepingService.update(testKeeping1.getId(), updateDTO));

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(25, updatedKeeping.getQuantity());
    }

    @Test
    void update_ShouldNotCheckDuplicate_WhenOnlyShelfChanged() {
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testKeeping1.getStorage().getId(), // Тот же склад
                testKeeping1.getItem().getId(),    // Тот же предмет
                testKeeping1.getQuantity(),
                "New Shelf Location",              // Только полка меняется
                null
        );

        // Не должно быть исключения дублирования
        assertDoesNotThrow(() -> keepingService.update(testKeeping1.getId(), updateDTO));

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals("New Shelf Location", updatedKeeping.getShelf());
    }

    // Тесты для метода delete
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

    // Тесты для метода findPage
    @Test
    void findPage_ShouldReturnAllKeepings_WhenNoFilters() {
        Page<KeepingDTO> result = keepingService.findPage(0, 10, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findPage_ShouldReturnFilteredByStorage_WhenStorageFilterApplied() {
        Page<KeepingDTO> result = keepingService.findPage(0, 10, testStorage1.getId(), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        KeepingDTO keepingDTO = result.getContent().get(0);
        assertEquals(testStorage1.getId(), keepingDTO.storageId());
        assertEquals(testKeeping1.getId(), keepingDTO.id());
    }

    @Test
    void findPage_ShouldReturnFilteredByItem_WhenItemFilterApplied() {
        Page<KeepingDTO> result = keepingService.findPage(0, 10, null, testItem2.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        KeepingDTO keepingDTO = result.getContent().get(0);
        assertEquals(testItem2.getId(), keepingDTO.itemId());
        assertEquals(testKeeping2.getId(), keepingDTO.id());
    }

    @Test
    void findPage_ShouldReturnFilteredByStorageAndItem_WhenBothFiltersApplied() {
        Page<KeepingDTO> result = keepingService.findPage(0, 10, testStorage1.getId(), testItem1.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        KeepingDTO keepingDTO = result.getContent().get(0);
        assertEquals(testStorage1.getId(), keepingDTO.storageId());
        assertEquals(testItem1.getId(), keepingDTO.itemId());
        assertEquals(testKeeping1.getId(), keepingDTO.id());
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatchingFilters() {
        Page<KeepingDTO> result = keepingService.findPage(0, 10, 999L, 999L);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
        Page<KeepingDTO> result = keepingService.findPage(0, 1, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements()); // Всего 2 элемента
        assertEquals(1, result.getContent().size()); // На странице 1 элемент
        assertEquals(2, result.getTotalPages()); // Всего 2 страницы
    }

    @Test
    void findPage_ShouldReturnSecondPage_WhenPageOneRequested() {
        Page<KeepingDTO> result = keepingService.findPage(1, 1, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size()); // На второй странице 1 элемент
        assertEquals(1, result.getNumber()); // Текущая страница 1
    }

    @Test
    void findPage_ShouldReturnSortedByLastUpdatedDesc() {
        // Создаем еще одну запись с более поздним временем обновления
        Keeping newKeeping = Keeping.builder()
                .storage(testStorage1)
                .item(testItem2)
                .quantity(3)
                .shelf("Z9")
                .lastUpdated(LocalDateTime.now()) // Самое позднее время
                .build();
        keepingRepository.save(newKeeping);

        Page<KeepingDTO> result = keepingService.findPage(0, 10, null, null);

        List<KeepingDTO> content = result.getContent();

        // Проверяем, что элементы отсортированы по lastUpdated в порядке убывания
        // Новая запись должна быть первой
        assertEquals(newKeeping.getId(), content.get(0).id());
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
        Page<KeepingDTO> result = keepingService.findPage(10, 10, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void update_ShouldHandleMinimumQuantity() {
        KeepingDTO updateDTO = new KeepingDTO(
                testKeeping1.getId(),
                testKeeping1.getStorage().getId(),
                testKeeping1.getItem().getId(),
                1, // Минимальное количество
                testKeeping1.getShelf(),
                null
        );

        assertDoesNotThrow(() -> keepingService.update(testKeeping1.getId(), updateDTO));

        Keeping updatedKeeping = keepingRepository.findById(testKeeping1.getId()).orElseThrow();
        assertEquals(1, updatedKeeping.getQuantity());
    }
}