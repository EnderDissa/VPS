package com.example.warehouse.service;

import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.MaintenanceStatus;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.ItemMaintenanceNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.repository.ItemMaintenanceRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemMaintenanceServiceImplIntegrationTest {

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
    private ItemMaintenanceServiceImpl itemMaintenanceService;

    @Autowired
    private ItemMaintenanceRepository itemMaintenanceRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User testTechnician;
    private User testTechnician2;
    private User nonExistentTechnician;
    private Item testItem;
    private Item testItem2;
    private Item nonExistentItem;
    private ItemMaintenance testMaintenance;

    @BeforeEach
    void setUp() {
        testTechnician = User.builder()
                .firstName("John")
                .lastName("Technician")
                .email("tech1@example.com")
                .role(RoleType.MANAGER)
                .createdAt(LocalDateTime.now())
                .build();
        testTechnician = userRepository.save(testTechnician);

        testTechnician2 = User.builder()
                .firstName("Jane")
                .lastName("Technician")
                .email("tech2@example.com")
                .role(RoleType.MANAGER)
                .createdAt(LocalDateTime.now())
                .build();
        testTechnician2 = userRepository.save(testTechnician2);

        nonExistentTechnician = User.builder()
                .id(999L)
                .firstName("Jane")
                .lastName("Technician")
                .email("tech2@example.com")
                .role(RoleType.MANAGER)
                .createdAt(LocalDateTime.now())
                .build();

        testItem = Item.builder()
                .name("Test Laptop")
                .description("High-performance laptop")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN-LAPTOP-001")
                .createdAt(LocalDateTime.now())
                .build();
        testItem = itemRepository.save(testItem);

        testItem2 = Item.builder()
                .name("Test Monitor")
                .description("4K Monitor")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.NEEDS_MAINTENANCE)
                .serialNumber("SN-MONITOR-001")
                .createdAt(LocalDateTime.now())
                .build();
        testItem2 = itemRepository.save(testItem2);

        nonExistentItem = Item.builder()
                .id(999L)
                .name("Test Monitor")
                .description("4K Monitor")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.NEEDS_MAINTENANCE)
                .serialNumber("SN-MONITOR-001")
                .createdAt(LocalDateTime.now())
                .build();

        testMaintenance = new ItemMaintenance();
        testMaintenance.setItem(testItem);
        testMaintenance.setTechnician(testTechnician);
        testMaintenance.setMaintenanceDate(LocalDateTime.now().minusDays(1));
        testMaintenance.setNextMaintenanceDate(LocalDateTime.now().plusMonths(6));
        testMaintenance.setCost(new BigDecimal("150.50"));
        testMaintenance.setDescription("Routine maintenance and cleaning");
        testMaintenance.setStatus(MaintenanceStatus.COMPLETED);
        testMaintenance = itemMaintenanceRepository.save(testMaintenance);
    }

    @Test
    void create_ShouldCreateItemMaintenance_WhenValidData() {
        ItemMaintenance newMaintenance = new ItemMaintenance(
                null,
                testItem2,
                testTechnician2,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusMonths(3),
                new BigDecimal("75.25"),
                "Display calibration",
                MaintenanceStatus.COMPLETED,
                null
        );

        ItemMaintenance result = itemMaintenanceService.create(newMaintenance);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testItem2.getId(), result.getItem().getId());
        assertEquals(testTechnician2.getId(), result.getTechnician().getId());
        assertEquals(new BigDecimal("75.25"), result.getCost());
        assertEquals("Display calibration", result.getDescription());
        assertEquals(MaintenanceStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCreatedAt());

        ItemMaintenance savedMaintenance = itemMaintenanceRepository.findById(result.getId()).orElseThrow();
        assertEquals(testItem2.getId(), savedMaintenance.getItem().getId());
        assertEquals(testTechnician2.getId(), savedMaintenance.getTechnician().getId());
    }

    @Test
    void create_ShouldThrowException_WhenItemNotFound() {
        ItemMaintenance maintenance = new ItemMaintenance(
                null,
                nonExistentItem,
                testTechnician,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(6),
                new BigDecimal("100.00"),
                "Test maintenance",
                MaintenanceStatus.PLANNED,
                null
        );

        assertThrows(ItemNotFoundException.class,
                () -> itemMaintenanceService.create(maintenance));
    }

    @Test
    void create_ShouldThrowException_WhenTechnicianNotFound() {
        ItemMaintenance maintenance = new ItemMaintenance(
                null,
                testItem,
                nonExistentTechnician,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(6),
                new BigDecimal("100.00"),
                "Test maintenance",
                MaintenanceStatus.PLANNED,
                null
        );

        assertThrows(UserNotFoundException.class,
                () -> itemMaintenanceService.create(maintenance));
    }

    @Test
    void create_ShouldSetDefaultStatus_WhenStatusNotProvided() {
        ItemMaintenance maintenanceWithoutStatus = ItemMaintenance.builder()
                .item(testItem2)
                .technician(testTechnician)
                .maintenanceDate(LocalDateTime.now())
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(new BigDecimal("50.00"))
                .description("Maintenance without status")
                .build();

        ItemMaintenance result = itemMaintenanceService.create(maintenanceWithoutStatus);

        assertEquals(MaintenanceStatus.COMPLETED, result.getStatus());
    }

    @Test
    void getById_ShouldReturnItemMaintenance_WhenExists() {
        ItemMaintenance result = itemMaintenanceService.getById(testMaintenance.getId());

        assertNotNull(result);
        assertEquals(testMaintenance.getId(), result.getId());
        assertEquals(testItem.getId(), result.getItem().getId());
        assertEquals(testTechnician.getId(), result.getTechnician().getId());
        assertEquals(new BigDecimal("150.50"), result.getCost());
        assertEquals(MaintenanceStatus.COMPLETED, result.getStatus());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(ItemMaintenanceNotFoundException.class,
                () -> itemMaintenanceService.getById(nonExistentId));
    }

    @Test
    void update_ShouldUpdateItemMaintenance_WhenValidData() {
        ItemMaintenance update = new ItemMaintenance(
                testMaintenance.getId(),
                testItem,
                testTechnician2,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusMonths(12),
                new BigDecimal("200.00"),
                "Extended warranty maintenance",
                MaintenanceStatus.IN_PROGRESS,
                testMaintenance.getCreatedAt()
        );

        itemMaintenanceService.update(testMaintenance.getId(), update);

        ItemMaintenance updatedMaintenance = itemMaintenanceRepository.findById(testMaintenance.getId()).orElseThrow();
        assertEquals(testTechnician2.getId(), updatedMaintenance.getTechnician().getId());
        assertEquals(new BigDecimal("200.00"), updatedMaintenance.getCost());
        assertEquals("Extended warranty maintenance", updatedMaintenance.getDescription());
        assertEquals(MaintenanceStatus.IN_PROGRESS, updatedMaintenance.getStatus());
    }

    @Test
    void update_ShouldUpdateItem_WhenItemChanged() {
        ItemMaintenance update = new ItemMaintenance(
                testMaintenance.getId(),
                testItem2,
                testTechnician,
                testMaintenance.getMaintenanceDate(),
                testMaintenance.getNextMaintenanceDate(),
                testMaintenance.getCost(),
                testMaintenance.getDescription(),
                testMaintenance.getStatus(),
                testMaintenance.getCreatedAt()
        );

        itemMaintenanceService.update(testMaintenance.getId(), update);

        ItemMaintenance updatedMaintenance = itemMaintenanceRepository.findById(testMaintenance.getId()).orElseThrow();
        assertEquals(testItem2.getId(), updatedMaintenance.getItem().getId());
    }

    @Test
    void update_ShouldThrowException_WhenItemMaintenanceNotFound() {
        Long nonExistentId = 999L;
        ItemMaintenance update = new ItemMaintenance(
                nonExistentId,
                testItem,
                testTechnician,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(6),
                new BigDecimal("100.00"),
                "Test update",
                MaintenanceStatus.PLANNED,
                null
        );

        assertThrows(ItemMaintenanceNotFoundException.class,
                () -> itemMaintenanceService.update(nonExistentId, update));
    }

    @Test
    void update_ShouldThrowException_WhenNewItemNotFound() {
        ItemMaintenance update = new ItemMaintenance(
                testMaintenance.getId(),
                nonExistentItem,
                testTechnician,
                testMaintenance.getMaintenanceDate(),
                testMaintenance.getNextMaintenanceDate(),
                testMaintenance.getCost(),
                testMaintenance.getDescription(),
                testMaintenance.getStatus(),
                testMaintenance.getCreatedAt()
        );

        assertThrows(ItemNotFoundException.class,
                () -> itemMaintenanceService.update(testMaintenance.getId(), update));
    }

    @Test
    void update_ShouldThrowException_WhenNewTechnicianNotFound() {
        ItemMaintenance update = new ItemMaintenance(
                testMaintenance.getId(),
                testItem,
                nonExistentTechnician,
                testMaintenance.getMaintenanceDate(),
                testMaintenance.getNextMaintenanceDate(),
                testMaintenance.getCost(),
                testMaintenance.getDescription(),
                testMaintenance.getStatus(),
                testMaintenance.getCreatedAt()
        );

        assertThrows(UserNotFoundException.class,
                () -> itemMaintenanceService.update(testMaintenance.getId(), update));
    }

    @Test
    void delete_ShouldDeleteItemMaintenance_WhenExists() {
        Long maintenanceId = testMaintenance.getId();

        itemMaintenanceService.delete(maintenanceId);

        assertFalse(itemMaintenanceRepository.existsById(maintenanceId));
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(ItemMaintenanceNotFoundException.class,
                () -> itemMaintenanceService.delete(nonExistentId));
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithItemIdFilter() {
        Page<ItemMaintenance> result = itemMaintenanceService.findPage(0, 10, testItem.getId(), null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(testItem.getId(), result.getContent().get(0).getItem().getId());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithStatusFilter() {
        Page<ItemMaintenance> result = itemMaintenanceService.findPage(0, 10, null, MaintenanceStatus.COMPLETED);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(MaintenanceStatus.COMPLETED, result.getContent().get(0).getStatus());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithBothFilters() {
        Page<ItemMaintenance> result = itemMaintenanceService.findPage(
                0, 10, testItem.getId(), MaintenanceStatus.COMPLETED);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);

        ItemMaintenance maintenance = result.getContent().get(0);
        assertEquals(testItem.getId(), maintenance.getItem().getId());
        assertEquals(MaintenanceStatus.COMPLETED, maintenance.getStatus());
    }

    @Test
    void findPage_ShouldReturnAll_WhenNoFilters() {
        Page<ItemMaintenance> result = itemMaintenanceService.findPage(0, 10, null, null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
    }

    @Test
    void findPage_ShouldReturnEmpty_WhenNoMatches() {
        Page<ItemMaintenance> result = itemMaintenanceService.findPage(0, 10, 999L, MaintenanceStatus.CANCELLED);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findPage_ShouldReturnPagedResults() {
        for (int i = 0; i < 5; i++) {
            ItemMaintenance maintenance = new ItemMaintenance();
            maintenance.setItem(testItem);
            maintenance.setTechnician(testTechnician);
            maintenance.setMaintenanceDate(LocalDateTime.now().minusDays(i));
            maintenance.setNextMaintenanceDate(LocalDateTime.now().plusMonths(6));
            maintenance.setCost(new BigDecimal("100.00"));
            maintenance.setDescription("Maintenance " + i);
            maintenance.setStatus(MaintenanceStatus.COMPLETED);
            itemMaintenanceRepository.save(maintenance);
        }

        Page<ItemMaintenance> result = itemMaintenanceService.findPage(0, 3, null, null);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertTrue(result.getTotalElements() >= 6);
    }

    @Test
    void findByTechnician_ShouldReturnTechnicianMaintenance() {
        Page<ItemMaintenance> result = itemMaintenanceService.findByTechnician(testTechnician.getId(), 0, 10);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(testTechnician.getId(), result.getContent().get(0).getTechnician().getId());
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        long count = itemMaintenanceService.countByStatus(MaintenanceStatus.COMPLETED);

        assertEquals(1, count);
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        itemMaintenanceService.updateStatus(testMaintenance.getId(), MaintenanceStatus.CANCELLED);

        ItemMaintenance updatedMaintenance = itemMaintenanceRepository.findById(testMaintenance.getId()).orElseThrow();
        assertEquals(MaintenanceStatus.CANCELLED, updatedMaintenance.getStatus());
    }

    @Test
    void updateStatus_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(ItemMaintenanceNotFoundException.class,
                () -> itemMaintenanceService.updateStatus(nonExistentId, MaintenanceStatus.COMPLETED));
    }

    @Test
    void create_ShouldHandleZeroCost() {
        ItemMaintenance maintenanceWithZeroCost = new ItemMaintenance(
                null,
                testItem2,
                testTechnician,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(6),
                BigDecimal.ZERO,
                "Warranty maintenance",
                MaintenanceStatus.COMPLETED,
                null
        );

        ItemMaintenance result = itemMaintenanceService.create(maintenanceWithZeroCost);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getCost());
    }

    @Test
    void create_ShouldHandleNullNextMaintenanceDate() {
        ItemMaintenance maintenanceWithoutNextDate = new ItemMaintenance(
                null,
                testItem2,
                testTechnician,
                LocalDateTime.now(),
                null,
                new BigDecimal("50.00"),
                "One-time repair",
                MaintenanceStatus.COMPLETED,
                null
        );

        ItemMaintenance result = itemMaintenanceService.create(maintenanceWithoutNextDate);

        assertNotNull(result);
        assertNull(result.getNextMaintenanceDate());
    }
}