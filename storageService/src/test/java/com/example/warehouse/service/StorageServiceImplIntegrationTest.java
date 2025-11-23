//package com.example.warehouse.service;
//
//import com.example.warehouse.entity.Storage;
//import com.example.warehouse.entity.Keeping;
//import com.example.warehouse.entity.Item;
//import com.example.warehouse.enumeration.ItemCondition;
//import com.example.warehouse.enumeration.ItemType;
//import com.example.warehouse.exception.DuplicateStorageException;
//import com.example.warehouse.exception.StorageNotFoundException;
//import com.example.warehouse.exception.StorageNotEmptyException;
//import com.example.warehouse.repository.StorageRepository;
//import com.example.warehouse.repository.KeepingRepository;
//import com.example.warehouse.repository.ItemRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.jdbc.Sql;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import java.time.LocalDateTime;
//import java.util.List;
//import static org.junit.jupiter.api.Assertions.*;
//
//@Testcontainers
//@SpringBootTest
//@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//class StorageServiceImplIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
//            .withDatabaseName("testdb")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired
//    private StorageServiceImpl storageService;
//
//    @Autowired
//    private StorageRepository storageRepository;
//
//    @Autowired
//    private KeepingRepository keepingRepository;
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    private Storage testStorage1;
//    private Storage testStorage2;
//    private Storage testStorage3;
//    private Item testItem;
//    private Keeping testKeeping;
//
//    @BeforeEach
//    void setUp() {
//        keepingRepository.deleteAll();
//        storageRepository.deleteAll();
//        itemRepository.deleteAll();
//
//        testStorage1 = Storage.builder()
//                .name("Main Warehouse")
//                .address("123 Main Street, City Center")
//                .capacity(1000)
//                .createdAt(LocalDateTime.now().minusDays(5))
//                .build();
//
//        testStorage2 = Storage.builder()
//                .name("Secondary Storage")
//                .address("456 Oak Avenue, Industrial Zone")
//                .capacity(500)
//                .createdAt(LocalDateTime.now().minusDays(3))
//                .build();
//
//        testStorage3 = Storage.builder()
//                .name("Tertiary Warehouse")
//                .address("789 Pine Road, Suburb")
//                .capacity(300)
//                .createdAt(LocalDateTime.now().minusDays(1))
//                .build();
//
//        testStorage1 = storageRepository.save(testStorage1);
//        testStorage2 = storageRepository.save(testStorage2);
//        testStorage3 = storageRepository.save(testStorage3);
//
//        testItem = Item.builder()
//                .name("Test Laptop")
//                .type(ItemType.ELECTRONICS)
//                .condition(ItemCondition.GOOD)
//                .serialNumber("SN123456")
//                .description("Test laptop for storage")
//                .createdAt(LocalDateTime.now())
//                .build();
//        testItem = itemRepository.save(testItem);
//
//        testKeeping = Keeping.builder()
//                .storage(testStorage1)
//                .item(testItem)
//                .quantity(5)
//                .shelf("A1")
//                .lastUpdated(LocalDateTime.now())
//                .build();
//        testKeeping = keepingRepository.save(testKeeping);
//    }
//
//    @Test
//    void create_ShouldCreateStorage_WhenValidData() {
//        Storage newStorage = Storage.builder()
//                .name("New Storage Facility")
//                .address("999 New Street, Business Park")
//                .capacity(750)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        Storage result = storageService.create(newStorage);
//
//        assertNotNull(result);
//        assertNotNull(result.getId());
//        assertEquals("New Storage Facility", result.getName());
//        assertEquals("999 New Street, Business Park", result.getAddress());
//        assertEquals(750, result.getCapacity());
//        assertNotNull(result.getCreatedAt());
//
//        Storage savedStorage = storageRepository.findById(result.getId()).orElseThrow();
//        assertEquals("New Storage Facility", savedStorage.getName());
//        assertEquals("999 New Street, Business Park", savedStorage.getAddress());
//    }
//
//    @Test
//    void create_ShouldCreateStorage_WhenZeroCapacity() {
//        Storage newStorage = Storage.builder()
//                .name("Zero Capacity Storage")
//                .address("Zero Address")
//                .capacity(0)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        Storage result = storageService.create(newStorage);
//
//        assertNotNull(result);
//        assertEquals(0, result.getCapacity());
//        assertEquals("Zero Capacity Storage", result.getName());
//    }
//
//    @Test
//    void create_ShouldCreateStorage_WhenNullCapacity() {
//        Storage newStorage = Storage.builder()
//                .name("Null Capacity Storage")
//                .address("Null Address")
//                .capacity(null)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        Storage result = storageService.create(newStorage);
//
//        assertNotNull(result);
//        assertNull(result.getCapacity());
//        assertEquals("Null Capacity Storage", result.getName());
//    }
//
//    @Test
//    void create_ShouldThrowDuplicateStorageException_WhenNameExists() {
//        Storage duplicateStorage = Storage.builder()
//                .name("Main Warehouse")
//                .address("Different Address")
//                .capacity(200)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        DuplicateStorageException exception = assertThrows(
//                DuplicateStorageException.class,
//                () -> storageService.create(duplicateStorage)
//        );
//
//        assertTrue(exception.getMessage().contains("Storage with name 'Main Warehouse' already exists"));
//    }
//
//    @Test
//    void getById_ShouldReturnStorage_WhenStorageExists() {
//        Storage result = storageService.getById(testStorage1.getId());
//
//        assertNotNull(result);
//        assertEquals(testStorage1.getId(), result.getId());
//        assertEquals(testStorage1.getName(), result.getName());
//        assertEquals(testStorage1.getAddress(), result.getAddress());
//        assertEquals(testStorage1.getCapacity(), result.getCapacity());
//        assertEquals(testStorage1.getCreatedAt(), result.getCreatedAt());
//    }
//
//    @Test
//    void getById_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
//        Long nonExistentId = 999L;
//
//        StorageNotFoundException exception = assertThrows(
//                StorageNotFoundException.class,
//                () -> storageService.getById(nonExistentId)
//        );
//
//        assertTrue(exception.getMessage().contains("Storage not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void update_ShouldUpdateStorage_WhenValidData() {
//        Storage updateStorage = Storage.builder()
//                .name("Updated Main Warehouse")
//                .address("Updated Address, New Location")
//                .capacity(1200)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        storageService.update(testStorage1.getId(), updateStorage);
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertEquals("Updated Main Warehouse", updatedStorage.getName());
//        assertEquals("Updated Address, New Location", updatedStorage.getAddress());
//        assertEquals(1200, updatedStorage.getCapacity());
//        assertEquals(testStorage1.getCreatedAt(), updatedStorage.getCreatedAt());
//    }
//
//    @Test
//    void update_ShouldUpdateStorage_WhenOnlyNameChanged() {
//        Storage updateStorage = Storage.builder()
//                .name("Only Name Updated")
//                .address(testStorage1.getAddress())
//                .capacity(testStorage1.getCapacity())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        storageService.update(testStorage1.getId(), updateStorage);
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertEquals("Only Name Updated", updatedStorage.getName());
//        assertEquals(testStorage1.getAddress(), updatedStorage.getAddress());
//        assertEquals(testStorage1.getCapacity(), updatedStorage.getCapacity());
//    }
//
//    @Test
//    void update_ShouldUpdateStorage_WhenOnlyAddressChanged() {
//        Storage updateStorage = Storage.builder()
//                .name(testStorage1.getName())
//                .address("Completely New Address")
//                .capacity(testStorage1.getCapacity())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        storageService.update(testStorage1.getId(), updateStorage);
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertEquals(testStorage1.getName(), updatedStorage.getName());
//        assertEquals("Completely New Address", updatedStorage.getAddress());
//        assertEquals(testStorage1.getCapacity(), updatedStorage.getCapacity());
//    }
//
//    @Test
//    void update_ShouldUpdateStorage_WhenOnlyCapacityChanged() {
//        Storage updateStorage = Storage.builder()
//                .name(testStorage1.getName())
//                .address(testStorage1.getAddress())
//                .capacity(2000)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        storageService.update(testStorage1.getId(), updateStorage);
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertEquals(testStorage1.getName(), updatedStorage.getName());
//        assertEquals(testStorage1.getAddress(), updatedStorage.getAddress());
//        assertEquals(2000, updatedStorage.getCapacity());
//    }
//
//    @Test
//    void update_ShouldUpdateStorage_WhenCapacitySetToNull() {
//        Storage updateStorage = Storage.builder()
//                .name(testStorage1.getName())
//                .address(testStorage1.getAddress())
//                .capacity(null)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        storageService.update(testStorage1.getId(), updateStorage);
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertNull(updatedStorage.getCapacity());
//        assertEquals(testStorage1.getName(), updatedStorage.getName());
//    }
//
//    @Test
//    void update_ShouldUpdateStorage_WhenCapacitySetToZero() {
//        Storage updateStorage = Storage.builder()
//                .name(testStorage1.getName())
//                .address(testStorage1.getAddress())
//                .capacity(0)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        storageService.update(testStorage1.getId(), updateStorage);
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertEquals(0, updatedStorage.getCapacity());
//    }
//
//    @Test
//    void update_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
//        Long nonExistentId = 999L;
//        Storage updateStorage = Storage.builder()
//                .name("Non-existent Storage")
//                .address("Address")
//                .capacity(100)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        StorageNotFoundException exception = assertThrows(
//                StorageNotFoundException.class,
//                () -> storageService.update(nonExistentId, updateStorage)
//        );
//
//        assertTrue(exception.getMessage().contains("Storage not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void update_ShouldThrowDuplicateStorageException_WhenNameTakenByOtherStorage() {
//        Storage updateStorage = Storage.builder()
//                .name("Secondary Storage")
//                .address(testStorage1.getAddress())
//                .capacity(testStorage1.getCapacity())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        DuplicateStorageException exception = assertThrows(
//                DuplicateStorageException.class,
//                () -> storageService.update(testStorage1.getId(), updateStorage)
//        );
//
//        assertTrue(exception.getMessage().contains("Storage with name 'Secondary Storage' already exists"));
//    }
//
//    @Test
//    void update_ShouldNotThrowException_WhenNameNotChanged() {
//        Storage updateStorage = Storage.builder()
//                .name(testStorage1.getName())
//                .address("Updated Address")
//                .capacity(1500)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        assertDoesNotThrow(() -> storageService.update(testStorage1.getId(), updateStorage));
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertEquals(testStorage1.getName(), updatedStorage.getName());
//        assertEquals("Updated Address", updatedStorage.getAddress());
//        assertEquals(1500, updatedStorage.getCapacity());
//    }
//
//    @Test
//    void delete_ShouldDeleteStorage_WhenStorageExistsAndEmpty() {
//        Long storageId = testStorage2.getId();
//
//        storageService.delete(storageId);
//
//        assertFalse(storageRepository.existsById(storageId));
//    }
//
//    @Test
//    void delete_ShouldThrowStorageNotFoundException_WhenStorageNotFound() {
//        Long nonExistentId = 999L;
//
//        StorageNotFoundException exception = assertThrows(
//                StorageNotFoundException.class,
//                () -> storageService.delete(nonExistentId)
//        );
//
//        assertTrue(exception.getMessage().contains("Storage not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void delete_ShouldThrowStorageNotEmptyException_WhenStorageHasItems() {
//        StorageNotEmptyException exception = assertThrows(
//                StorageNotEmptyException.class,
//                () -> storageService.delete(testStorage1.getId())
//        );
//
//        assertTrue(exception.getMessage().contains("Cannot delete storage with ID: " + testStorage1.getId()));
//        assertTrue(exception.getMessage().contains("It contains 1 items."));
//    }
//
//    @Test
//    void delete_ShouldThrowStorageNotEmptyException_WhenStorageHasMultipleItems() {
//        Item anotherItem = Item.builder()
//                .name("Another Item")
//                .type(ItemType.FURNITURE)
//                .condition(ItemCondition.NEW)
//                .serialNumber("SN789012")
//                .description("Another test item")
//                .createdAt(LocalDateTime.now())
//                .build();
//        anotherItem = itemRepository.save(anotherItem);
//
//        Keeping anotherKeeping = Keeping.builder()
//                .storage(testStorage1)
//                .item(anotherItem)
//                .quantity(3)
//                .shelf("B2")
//                .lastUpdated(LocalDateTime.now())
//                .build();
//        keepingRepository.save(anotherKeeping);
//
//        StorageNotEmptyException exception = assertThrows(
//                StorageNotEmptyException.class,
//                () -> storageService.delete(testStorage1.getId())
//        );
//
//        assertTrue(exception.getMessage().contains("It contains 2 items."));
//    }
//
//    @Test
//    void findPage_ShouldReturnAllStorages_WhenNoNameFilter() {
//        Page<Storage> result = storageService.findPage(0, 10, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(3, result.getContent().size());
//    }
//
//    @Test
//    void findPage_ShouldReturnAllStorages_WhenEmptyNameFilter() {
//        Page<Storage> result = storageService.findPage(0, 10, "");
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(3, result.getContent().size());
//    }
//
//    @Test
//    void findPage_ShouldReturnAllStorages_WhenBlankNameFilter() {
//        Page<Storage> result = storageService.findPage(0, 10, "   ");
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(3, result.getContent().size());
//    }
//
//    @Test
//    void findPage_ShouldReturnFilteredStorages_WhenNameFilterApplied() {
//        Page<Storage> result = storageService.findPage(0, 10, "warehouse");
//
//        assertNotNull(result);
//        assertEquals(2, result.getTotalElements());
//
//        List<Storage> content = result.getContent();
//        assertTrue(content.stream().allMatch(storage ->
//                storage.getName().toLowerCase().contains("warehouse")));
//    }
//
//    @Test
//    void findPage_ShouldReturnFilteredStorages_WhenNameFilterCaseInsensitive() {
//        Page<Storage> result = storageService.findPage(0, 10, "WAREHOUSE");
//
//        assertNotNull(result);
//        assertEquals(2, result.getTotalElements());
//        assertTrue(result.getContent().stream().allMatch(storage ->
//                storage.getName().toLowerCase().contains("warehouse")));
//    }
//
//    @Test
//    void findPage_ShouldReturnFilteredStorages_WhenPartialNameMatch() {
//        Page<Storage> result = storageService.findPage(0, 10, "main");
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        assertEquals("Main Warehouse", result.getContent().get(0).getName());
//    }
//
//    @Test
//    void findPage_ShouldReturnEmptyPage_WhenNoMatchingName() {
//        Page<Storage> result = storageService.findPage(0, 10, "nonexistent");
//
//        assertNotNull(result);
//        assertEquals(0, result.getTotalElements());
//        assertTrue(result.getContent().isEmpty());
//    }
//
//    @Test
//    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
//        Page<Storage> result = storageService.findPage(0, 2, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(2, result.getContent().size());
//        assertEquals(2, result.getTotalPages());
//    }
//
//    @Test
//    void findPage_ShouldReturnSecondPage_WhenPageOneRequested() {
//        Page<Storage> result = storageService.findPage(1, 2, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(1, result.getContent().size());
//        assertEquals(1, result.getNumber());
//    }
//
//    @Test
//    void findPage_ShouldReturnSortedByCreatedAtDesc() {
//        Page<Storage> result = storageService.findPage(0, 10, null);
//
//        List<Storage> content = result.getContent();
//
//        assertEquals(testStorage3.getId(), content.get(0).getId());
//        assertEquals(testStorage2.getId(), content.get(1).getId());
//        assertEquals(testStorage1.getId(), content.get(2).getId());
//    }
//
//    @Test
//    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
//        Page<Storage> result = storageService.findPage(10, 10, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertTrue(result.getContent().isEmpty());
//    }
//
//    @Test
//    void findPage_ShouldHandleZeroPageSize() {
//
//        assertThrows(IllegalArgumentException.class, () -> storageService.findPage(0, 0, null));
//    }
//
//    @Test
//    void create_ShouldHandleLongNamesAndAddresses() {
//        Storage newStorage = Storage.builder()
//                .name("A".repeat(255))
//                .address("B".repeat(1000))
//                .capacity(100)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        Storage result = storageService.create(newStorage);
//
//        assertNotNull(result);
//        assertEquals(255, result.getName().length());
//        assertEquals(1000, result.getAddress().length());
//    }
//
//    @Test
//    void update_ShouldHandleLongNamesAndAddresses() {
//        Storage updateStorage = Storage.builder()
//                .name("Updated " + "A".repeat(200))
//                .address("Updated " + "B".repeat(500))
//                .capacity(500)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        assertDoesNotThrow(() -> storageService.update(testStorage1.getId(), updateStorage));
//
//        Storage updatedStorage = storageRepository.findById(testStorage1.getId()).orElseThrow();
//        assertEquals(208, updatedStorage.getName().length());
//        assertEquals(508, updatedStorage.getAddress().length());
//    }
//
//    @Test
//    void delete_ShouldWorkAfterRemovingAllItems() {
//        keepingRepository.deleteAll();
//
//        assertDoesNotThrow(() -> storageService.delete(testStorage1.getId()));
//        assertFalse(storageRepository.existsById(testStorage1.getId()));
//    }
//
//    @Test
//    void getById_ShouldReturnConsistentData() {
//        Storage result1 = storageService.getById(testStorage1.getId());
//        Storage result2 = storageService.getById(testStorage1.getId());
//
//        assertEquals(result1.getId(), result2.getId());
//        assertEquals(result1.getName(), result2.getName());
//        assertEquals(result1.getAddress(), result2.getAddress());
//        assertEquals(result1.getCapacity(), result2.getCapacity());
//        assertEquals(result1.getCreatedAt(), result2.getCreatedAt());
//    }
//}