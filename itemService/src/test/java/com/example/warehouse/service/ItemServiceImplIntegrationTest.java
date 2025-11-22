//package com.example.warehouse.service;
//
//import com.example.warehouse.entity.Item;
//import com.example.warehouse.enumeration.ItemCondition;
//import com.example.warehouse.enumeration.ItemType;
//import com.example.warehouse.exception.ItemNotFoundException;
//import com.example.warehouse.exception.DuplicateSerialNumberException;
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
//class ItemServiceImplIntegrationTest {
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
//    private ItemServiceImpl itemService;
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    private Item testItem1;
//    private Item testItem2;
//    private Item testItem3;
//
//    @BeforeEach
//    void setUp() {
//        itemRepository.deleteAll();
//
//        testItem1 = Item.builder()
//                .name("Laptop Dell XPS")
//                .type(ItemType.ELECTRONICS)
//                .condition(ItemCondition.GOOD)
//                .serialNumber("SN123456")
//                .description("High-performance laptop")
//                .createdAt(LocalDateTime.now().minusDays(5))
//                .build();
//
//        testItem2 = Item.builder()
//                .name("Office Chair")
//                .type(ItemType.FURNITURE)
//                .condition(ItemCondition.NEW)
//                .serialNumber("SN789012")
//                .description("Ergonomic office chair")
//                .createdAt(LocalDateTime.now().minusDays(3))
//                .build();
//
//        testItem3 = Item.builder()
//                .name("Broken Monitor")
//                .type(ItemType.ELECTRONICS)
//                .condition(ItemCondition.UNDER_REPAIR)
//                .serialNumber("SN345678")
//                .description("Needs repair")
//                .createdAt(LocalDateTime.now().minusDays(1))
//                .build();
//
//        testItem1 = itemRepository.save(testItem1);
//        testItem2 = itemRepository.save(testItem2);
//        testItem3 = itemRepository.save(testItem3);
//    }
//
//    @Test
//    void create_ShouldCreateItem_WhenValidData() {
//        Item newItem = new Item(
//                null,
//                "New Tablet",
//                ItemType.ELECTRONICS,
//                ItemCondition.NEW,
//                "SN999999",
//                "Brand new tablet",
//                null
//        );
//
//        Item result = itemService.create(newItem);
//
//        assertNotNull(result);
//        assertNotNull(result.getId());
//        assertEquals("New Tablet", result.getName());
//        assertEquals(ItemType.ELECTRONICS, result.getType());
//        assertEquals(ItemCondition.NEW, result.getCondition());
//        assertEquals("SN999999", result.getSerialNumber());
//        assertEquals("Brand new tablet", result.getDescription());
//
//        Item savedItem = itemRepository.findById(result.getId()).orElseThrow();
//        assertEquals("New Tablet", savedItem.getName());
//    }
//
//    @Test
//    void create_ShouldCreateItem_WhenSerialNumberIsNull() {
//        Item newItem = new Item(
//                null,
//                "Item Without Serial",
//                ItemType.FURNITURE,
//                ItemCondition.GOOD,
//                null,
//                "No serial number",
//                null
//        );
//
//        Item result = itemService.create(newItem);
//
//        assertNotNull(result);
//        assertNull(result.getSerialNumber());
//        assertEquals("Item Without Serial", result.getName());
//    }
//
//    @Test
//    void create_ShouldCreateItem_WhenSerialNumberIsEmpty() {
//        Item newItem = new Item(
//                null,
//                "Item With Empty Serial",
//                ItemType.FURNITURE,
//                ItemCondition.GOOD,
//                "",
//                "Empty serial number",
//                null
//        );
//
//        Item result = itemService.create(newItem);
//
//        assertNotNull(result);
//        assertEquals("", result.getSerialNumber());
//    }
//
//    @Test
//    void create_ShouldThrowDuplicateSerialNumberException_WhenSerialNumberExists() {
//        Item newItem = new Item(
//                null,
//                "Another Laptop",
//                ItemType.ELECTRONICS,
//                ItemCondition.GOOD,
//                "SN123456",
//                "Duplicate serial",
//                null
//        );
//
//        DuplicateSerialNumberException exception = assertThrows(
//                DuplicateSerialNumberException.class,
//                () -> itemService.create(newItem)
//        );
//
//        assertTrue(exception.getMessage().contains("Item with serial number 'SN123456' already exists"));
//    }
//
//    @Test
//    void getById_ShouldReturnItem_WhenItemExists() {
//        Item result = itemService.getById(testItem1.getId());
//
//        assertNotNull(result);
//        assertEquals(testItem1.getId(), result.getId());
//        assertEquals(testItem1.getName(), result.getName());
//        assertEquals(testItem1.getType(), result.getType());
//        assertEquals(testItem1.getCondition(), result.getCondition());
//        assertEquals(testItem1.getSerialNumber(), result.getSerialNumber());
//        assertEquals(testItem1.getDescription(), result.getDescription());
//    }
//
//    @Test
//    void getById_ShouldThrowItemNotFoundException_WhenItemNotFound() {
//        Long nonExistentId = 999L;
//
//        ItemNotFoundException exception = assertThrows(
//                ItemNotFoundException.class,
//                () -> itemService.getById(nonExistentId)
//        );
//
//        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void update_ShouldUpdateItem_WhenValidData() {
//        Item update = new Item(
//                testItem1.getId(),
//                "Updated Laptop Name",
//                ItemType.ELECTRONICS,
//                ItemCondition.EXCELLENT,
//                "SN123456",
//                "Updated description",
//                null
//        );
//
//        itemService.update(testItem1.getId(), update);
//
//        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
//        assertEquals("Updated Laptop Name", updatedItem.getName());
//        assertEquals(ItemCondition.EXCELLENT, updatedItem.getCondition());
//        assertEquals("Updated description", updatedItem.getDescription());
//    }
//
//    @Test
//    void update_ShouldUpdateItem_WhenSerialNumberChangedToUnique() {
//        Item update = new Item(
//                testItem1.getId(),
//                testItem1.getName(),
//                testItem1.getType(),
//                testItem1.getCondition(),
//                "SN_NEW_UNIQUE",
//                testItem1.getDescription(),
//                null
//        );
//
//        itemService.update(testItem1.getId(), update);
//
//        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
//        assertEquals("SN_NEW_UNIQUE", updatedItem.getSerialNumber());
//    }
//
//    @Test
//    void update_ShouldUpdateItem_WhenSerialNumberSetToNull() {
//        Item update = new Item(
//                testItem1.getId(),
//                testItem1.getName(),
//                testItem1.getType(),
//                testItem1.getCondition(),
//                null,
//                testItem1.getDescription(),
//                null
//        );
//
//        itemService.update(testItem1.getId(), update);
//
//        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
//        assertNull(updatedItem.getSerialNumber());
//    }
//
//    @Test
//    void update_ShouldThrowItemNotFoundException_WhenItemNotFound() {
//        Long nonExistentId = 999L;
//        Item update = new Item(
//                nonExistentId,
//                "Non-existent Item",
//                ItemType.ELECTRONICS,
//                ItemCondition.GOOD,
//                "SN000000",
//                "Description",
//                null
//        );
//
//        ItemNotFoundException exception = assertThrows(
//                ItemNotFoundException.class,
//                () -> itemService.update(nonExistentId, update)
//        );
//
//        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void update_ShouldThrowDuplicateSerialNumberException_WhenSerialNumberExistsOnOtherItem() {
//        Item update = new Item(
//                testItem1.getId(),
//                testItem1.getName(),
//                testItem1.getType(),
//                testItem1.getCondition(),
//                "SN789012",
//                testItem1.getDescription(),
//                null
//        );
//
//        DuplicateSerialNumberException exception = assertThrows(
//                DuplicateSerialNumberException.class,
//                () -> itemService.update(testItem1.getId(), update)
//        );
//
//        assertTrue(exception.getMessage().contains("Item with serial number 'SN789012' already exists"));
//    }
//
//    @Test
//    void update_ShouldNotCheckSerialNumber_WhenSerialNumberNotChanged() {
//        Item update = new Item(
//                testItem1.getId(),
//                "Updated Name",
//                testItem1.getType(),
//                testItem1.getCondition(),
//                testItem1.getSerialNumber(),
//                "Updated description",
//                null
//        );
//
//        assertDoesNotThrow(() -> itemService.update(testItem1.getId(), update));
//
//        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
//        assertEquals("Updated Name", updatedItem.getName());
//        assertEquals("SN123456", updatedItem.getSerialNumber());
//    }
//
//    @Test
//    void delete_ShouldDeleteItem_WhenItemExists() {
//        Long itemId = testItem1.getId();
//
//        itemService.delete(itemId);
//
//        assertFalse(itemRepository.existsById(itemId));
//    }
//
//    @Test
//    void delete_ShouldThrowItemNotFoundException_WhenItemNotFound() {
//        Long nonExistentId = 999L;
//
//        ItemNotFoundException exception = assertThrows(
//                ItemNotFoundException.class,
//                () -> itemService.delete(nonExistentId)
//        );
//
//        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void findPage_ShouldReturnAllItems_WhenNoFilters() {
//        Page<Item> result = itemService.findPage(0, 10, null, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(3, result.getContent().size());
//    }
//
//    @Test
//    void findPage_ShouldReturnFilteredByType_WhenTypeFilterApplied() {
//        Page<Item> result = itemService.findPage(0, 10, ItemType.ELECTRONICS, null);
//
//        assertNotNull(result);
//        assertEquals(2, result.getTotalElements());
//
//        List<Item> content = result.getContent();
//        assertTrue(content.stream().allMatch(item -> item.getType() == ItemType.ELECTRONICS));
//    }
//
//    @Test
//    void findPage_ShouldReturnFilteredByCondition_WhenConditionFilterApplied() {
//        Page<Item> result = itemService.findPage(0, 10, null, ItemCondition.NEW);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//
//        List<Item> content = result.getContent();
//        assertTrue(content.stream().allMatch(item -> item.getCondition() == ItemCondition.NEW));
//    }
//
//    @Test
//    void findPage_ShouldReturnFilteredByTypeAndCondition_WhenBothFiltersApplied() {
//        Page<Item> result = itemService.findPage(0, 10, ItemType.ELECTRONICS, ItemCondition.UNDER_REPAIR);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//
//        Item item = result.getContent().get(0);
//        assertEquals(ItemType.ELECTRONICS, item.getType());
//        assertEquals(ItemCondition.UNDER_REPAIR, item.getCondition());
//    }
//
//    @Test
//    void findPage_ShouldReturnEmptyPage_WhenNoMatchingFilters() {
//        Page<Item> result = itemService.findPage(0, 10, ItemType.FURNITURE, ItemCondition.UNDER_REPAIR);
//
//        assertNotNull(result);
//        assertEquals(0, result.getTotalElements());
//        assertTrue(result.getContent().isEmpty());
//    }
//
//    @Test
//    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
//        Page<Item> result = itemService.findPage(0, 2, null, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(2, result.getContent().size());
//        assertEquals(2, result.getTotalPages());
//    }
//
//    @Test
//    void findPage_ShouldReturnSecondPage_WhenPageOneRequested() {
//        Page<Item> result = itemService.findPage(1, 2, null, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertEquals(1, result.getContent().size());
//        assertEquals(1, result.getNumber());
//    }
//
//    @Test
//    void findPage_ShouldReturnSortedByCreatedAtDesc() {
//        Page<Item> result = itemService.findPage(0, 10, null, null);
//
//        List<Item> content = result.getContent();
//
//        assertEquals(testItem3.getId(), content.get(0).getId());
//        assertEquals(testItem2.getId(), content.get(1).getId());
//        assertEquals(testItem1.getId(), content.get(2).getId());
//    }
//
//    @Test
//    void findAvailable_ShouldReturnAllItems_WhenNoFiltersAndNoCursor() {
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, null, null, null, 10
//        );
//
//        assertNotNull(result);
//        assertEquals(3, result.size());
//    }
//
//    @Test
//    void findAvailable_ShouldReturnLimitedItems_WhenLimitSpecified() {
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, null, null, null, 2
//        );
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//    }
//
//    @Test
//    void findAvailable_ShouldReturnItemsWithCursor_WhenCursorSpecified() {
//        Long cursorId = testItem1.getId();
//
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, null, null, cursorId, 10
//        );
//
//        assertNotNull(result);
//        assertTrue(result.stream().allMatch(item -> item.getId() > cursorId));
//    }
//
//    @Test
//    void findAvailable_ShouldReturnFilteredByType_WhenTypeFilterApplied() {
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, ItemType.ELECTRONICS, null, null, 10
//        );
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.stream().allMatch(item -> item.getType() == ItemType.ELECTRONICS));
//    }
//
//    @Test
//    void findAvailable_ShouldReturnFilteredByCondition_WhenConditionFilterApplied() {
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, null, ItemCondition.NEW, null, 10
//        );
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(ItemCondition.NEW, result.get(0).getCondition());
//    }
//
//    @Test
//    void findAvailable_ShouldReturnFilteredByTypeAndCondition_WhenBothFiltersApplied() {
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, ItemType.ELECTRONICS, ItemCondition.GOOD, null, 10
//        );
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(ItemType.ELECTRONICS, result.get(0).getType());
//        assertEquals(ItemCondition.GOOD, result.get(0).getCondition());
//    }
//
//    @Test
//    void findAvailable_ShouldReturnEmptyList_WhenNoMatchingFilters() {
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, ItemType.FURNITURE, ItemCondition.UNDER_REPAIR, null, 10
//        );
//
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void findAvailable_ShouldReturnItemsWithCursorAndTypeFilter() {
//        Long cursorId = testItem1.getId();
//
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, ItemType.ELECTRONICS, null, cursorId, 10
//        );
//
//        assertNotNull(result);
//        assertTrue(result.stream().allMatch(item ->
//                item.getId() > cursorId && item.getType() == ItemType.ELECTRONICS
//        ));
//    }
//
//    @Test
//    void findAvailable_ShouldReturnSortedByIdAsc() {
//        List<Item> result = itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, null, null, null, 10
//        );
//
//        for (int i = 0; i < result.size() - 1; i++) {
//            assertTrue(result.get(i).getId() < result.get(i + 1).getId());
//        }
//    }
//
//    @Test
//    void findAvailable_ShouldReturnEmptyList_WhenLimitZero() {
//        assertThrows(IllegalArgumentException.class, () -> itemService.findAvailable(
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(10),
//                null, null, null, null, 0
//        ));
//    }
//
//    @Test
//    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
//        Page<Item> result = itemService.findPage(10, 10, null, null);
//
//        assertNotNull(result);
//        assertEquals(3, result.getTotalElements());
//        assertTrue(result.getContent().isEmpty());
//    }
//}
