package com.example.warehouse.service;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.DuplicateSerialNumberException;
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
class ItemServiceImplIntegrationTest {

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
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem1;
    private Item testItem2;
    private Item testItem3;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        testItem1 = Item.builder()
                .name("Laptop Dell XPS")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .description("High-performance laptop")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        testItem2 = Item.builder()
                .name("Office Chair")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.NEW)
                .serialNumber("SN789012")
                .description("Ergonomic office chair")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        testItem3 = Item.builder()
                .name("Broken Monitor")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.UNDER_REPAIR)
                .serialNumber("SN345678")
                .description("Needs repair")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        testItem1 = itemRepository.save(testItem1);
        testItem2 = itemRepository.save(testItem2);
        testItem3 = itemRepository.save(testItem3);
    }

    @Test
    void create_ShouldCreateItem_WhenValidData() {
        ItemDTO newItemDTO = new ItemDTO(
                null,
                "New Tablet",
                ItemType.ELECTRONICS,
                ItemCondition.NEW,
                "SN999999",
                "Brand new tablet",
                null
        );

        ItemDTO result = itemService.create(newItemDTO);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("New Tablet", result.name());
        assertEquals(ItemType.ELECTRONICS, result.type());
        assertEquals(ItemCondition.NEW, result.condition());
        assertEquals("SN999999", result.serialNumber());
        assertEquals("Brand new tablet", result.description());

        Item savedItem = itemRepository.findById(result.id()).orElseThrow();
        assertEquals("New Tablet", savedItem.getName());
    }

    @Test
    void create_ShouldCreateItem_WhenSerialNumberIsNull() {
        ItemDTO newItemDTO = new ItemDTO(
                null,
                "Item Without Serial",
                ItemType.FURNITURE,
                ItemCondition.GOOD,
                null,
                "No serial number",
                null
        );

        ItemDTO result = itemService.create(newItemDTO);

        assertNotNull(result);
        assertNull(result.serialNumber());
        assertEquals("Item Without Serial", result.name());
    }

    @Test
    void create_ShouldCreateItem_WhenSerialNumberIsEmpty() {
        ItemDTO newItemDTO = new ItemDTO(
                null,
                "Item With Empty Serial",
                ItemType.FURNITURE,
                ItemCondition.GOOD,
                "",
                "Empty serial number",
                null
        );

        ItemDTO result = itemService.create(newItemDTO);

        assertNotNull(result);
        assertEquals("", result.serialNumber());
    }

    @Test
    void create_ShouldThrowDuplicateSerialNumberException_WhenSerialNumberExists() {
        ItemDTO newItemDTO = new ItemDTO(
                null,
                "Another Laptop",
                ItemType.ELECTRONICS,
                ItemCondition.GOOD,
                "SN123456",
                "Duplicate serial",
                null
        );

        DuplicateSerialNumberException exception = assertThrows(
                DuplicateSerialNumberException.class,
                () -> itemService.create(newItemDTO)
        );

        assertTrue(exception.getMessage().contains("Item with serial number 'SN123456' already exists"));
    }

    @Test
    void getById_ShouldReturnItem_WhenItemExists() {
        ItemDTO result = itemService.getById(testItem1.getId());

        assertNotNull(result);
        assertEquals(testItem1.getId(), result.id());
        assertEquals(testItem1.getName(), result.name());
        assertEquals(testItem1.getType(), result.type());
        assertEquals(testItem1.getCondition(), result.condition());
        assertEquals(testItem1.getSerialNumber(), result.serialNumber());
        assertEquals(testItem1.getDescription(), result.description());
    }

    @Test
    void getById_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        Long nonExistentId = 999L;

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.getById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldUpdateItem_WhenValidData() {
        ItemDTO updateDTO = new ItemDTO(
                testItem1.getId(),
                "Updated Laptop Name",
                ItemType.ELECTRONICS,
                ItemCondition.EXCELLENT,
                "SN123456",
                "Updated description",
                null
        );

        itemService.update(testItem1.getId(), updateDTO);

        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
        assertEquals("Updated Laptop Name", updatedItem.getName());
        assertEquals(ItemCondition.EXCELLENT, updatedItem.getCondition());
        assertEquals("Updated description", updatedItem.getDescription());
    }

    @Test
    void update_ShouldUpdateItem_WhenSerialNumberChangedToUnique() {
        ItemDTO updateDTO = new ItemDTO(
                testItem1.getId(),
                testItem1.getName(),
                testItem1.getType(),
                testItem1.getCondition(),
                "SN_NEW_UNIQUE",
                testItem1.getDescription(),
                null
        );

        itemService.update(testItem1.getId(), updateDTO);

        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
        assertEquals("SN_NEW_UNIQUE", updatedItem.getSerialNumber());
    }

    @Test
    void update_ShouldUpdateItem_WhenSerialNumberSetToNull() {
        ItemDTO updateDTO = new ItemDTO(
                testItem1.getId(),
                testItem1.getName(),
                testItem1.getType(),
                testItem1.getCondition(),
                null,
                testItem1.getDescription(),
                null
        );

        itemService.update(testItem1.getId(), updateDTO);

        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
        assertNull(updatedItem.getSerialNumber());
    }

    @Test
    void update_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        Long nonExistentId = 999L;
        ItemDTO updateDTO = new ItemDTO(
                nonExistentId,
                "Non-existent Item",
                ItemType.ELECTRONICS,
                ItemCondition.GOOD,
                "SN000000",
                "Description",
                null
        );

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.update(nonExistentId, updateDTO)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldThrowDuplicateSerialNumberException_WhenSerialNumberExistsOnOtherItem() {
        ItemDTO updateDTO = new ItemDTO(
                testItem1.getId(),
                testItem1.getName(),
                testItem1.getType(),
                testItem1.getCondition(),
                "SN789012",
                testItem1.getDescription(),
                null
        );

        DuplicateSerialNumberException exception = assertThrows(
                DuplicateSerialNumberException.class,
                () -> itemService.update(testItem1.getId(), updateDTO)
        );

        assertTrue(exception.getMessage().contains("Item with serial number 'SN789012' already exists"));
    }

    @Test
    void update_ShouldNotCheckSerialNumber_WhenSerialNumberNotChanged() {
        ItemDTO updateDTO = new ItemDTO(
                testItem1.getId(),
                "Updated Name",
                testItem1.getType(),
                testItem1.getCondition(),
                testItem1.getSerialNumber(),
                "Updated description",
                null
        );

        assertDoesNotThrow(() -> itemService.update(testItem1.getId(), updateDTO));

        Item updatedItem = itemRepository.findById(testItem1.getId()).orElseThrow();
        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("SN123456", updatedItem.getSerialNumber());
    }

    @Test
    void delete_ShouldDeleteItem_WhenItemExists() {
        Long itemId = testItem1.getId();

        itemService.delete(itemId);

        assertFalse(itemRepository.existsById(itemId));
    }

    @Test
    void delete_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        Long nonExistentId = 999L;

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.delete(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentId));
    }

    @Test
    void findPage_ShouldReturnAllItems_WhenNoFilters() {
        Page<ItemDTO> result = itemService.findPage(0, 10, null, null);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void findPage_ShouldReturnFilteredByType_WhenTypeFilterApplied() {
        Page<ItemDTO> result = itemService.findPage(0, 10, ItemType.ELECTRONICS, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        List<ItemDTO> content = result.getContent();
        assertTrue(content.stream().allMatch(item -> item.type() == ItemType.ELECTRONICS));
    }

    @Test
    void findPage_ShouldReturnFilteredByCondition_WhenConditionFilterApplied() {
        Page<ItemDTO> result = itemService.findPage(0, 10, null, ItemCondition.NEW);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        List<ItemDTO> content = result.getContent();
        assertTrue(content.stream().allMatch(item -> item.condition() == ItemCondition.NEW));
    }

    @Test
    void findPage_ShouldReturnFilteredByTypeAndCondition_WhenBothFiltersApplied() {
        Page<ItemDTO> result = itemService.findPage(0, 10, ItemType.ELECTRONICS, ItemCondition.UNDER_REPAIR);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        ItemDTO item = result.getContent().get(0);
        assertEquals(ItemType.ELECTRONICS, item.type());
        assertEquals(ItemCondition.UNDER_REPAIR, item.condition());
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatchingFilters() {
        Page<ItemDTO> result = itemService.findPage(0, 10, ItemType.FURNITURE, ItemCondition.UNDER_REPAIR);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
        Page<ItemDTO> result = itemService.findPage(0, 2, null, null);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void findPage_ShouldReturnSecondPage_WhenPageOneRequested() {
        Page<ItemDTO> result = itemService.findPage(1, 2, null, null);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
    }

    @Test
    void findPage_ShouldReturnSortedByCreatedAtDesc() {
        Page<ItemDTO> result = itemService.findPage(0, 10, null, null);

        List<ItemDTO> content = result.getContent();

        assertEquals(testItem3.getId(), content.get(0).id());
        assertEquals(testItem2.getId(), content.get(1).id());
        assertEquals(testItem1.getId(), content.get(2).id());
    }

    @Test
    void findAvailable_ShouldReturnAllItems_WhenNoFiltersAndNoCursor() {
        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, null, null, null, 10
        );

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void findAvailable_ShouldReturnLimitedItems_WhenLimitSpecified() {
        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, null, null, null, 2
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findAvailable_ShouldReturnItemsWithCursor_WhenCursorSpecified() {
        Long cursorId = testItem1.getId();

        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, null, null, cursorId, 10
        );

        assertNotNull(result);
        assertTrue(result.stream().allMatch(item -> item.id() > cursorId));
    }

    @Test
    void findAvailable_ShouldReturnFilteredByType_WhenTypeFilterApplied() {
        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, ItemType.ELECTRONICS, null, null, 10
        );

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> item.type() == ItemType.ELECTRONICS));
    }

    @Test
    void findAvailable_ShouldReturnFilteredByCondition_WhenConditionFilterApplied() {
        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, null, ItemCondition.NEW, null, 10
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ItemCondition.NEW, result.get(0).condition());
    }

    @Test
    void findAvailable_ShouldReturnFilteredByTypeAndCondition_WhenBothFiltersApplied() {
        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, ItemType.ELECTRONICS, ItemCondition.GOOD, null, 10
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ItemType.ELECTRONICS, result.get(0).type());
        assertEquals(ItemCondition.GOOD, result.get(0).condition());
    }

    @Test
    void findAvailable_ShouldReturnEmptyList_WhenNoMatchingFilters() {
        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, ItemType.FURNITURE, ItemCondition.UNDER_REPAIR, null, 10
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAvailable_ShouldReturnItemsWithCursorAndTypeFilter() {
        Long cursorId = testItem1.getId();

        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, ItemType.ELECTRONICS, null, cursorId, 10
        );

        assertNotNull(result);
        assertTrue(result.stream().allMatch(item ->
                item.id() > cursorId && item.type() == ItemType.ELECTRONICS
        ));
    }

    @Test
    void findAvailable_ShouldReturnSortedByIdAsc() {
        List<ItemDTO> result = itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, null, null, null, 10
        );

        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).id() < result.get(i + 1).id());
        }
    }

    @Test
    void findAvailable_ShouldReturnEmptyList_WhenLimitZero() {
        assertThrows(IllegalArgumentException.class, () -> itemService.findAvailable(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                null, null, null, null, 0
        ));
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
        Page<ItemDTO> result = itemService.findPage(10, 10, null, null);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
