package com.example.warehouse.service;

import com.example.warehouse.entity.Transportation;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.enumeration.*;
import com.example.warehouse.exception.TransportationNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.VehicleNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.repository.TransportationRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.VehicleRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.repository.StorageRepository;
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
class TransportationServiceImplIntegrationTest {

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
    private TransportationServiceImpl transportationService;

    @Autowired
    private TransportationRepository transportationRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageRepository storageRepository;

    private Item testItem1;
    private Item testItem2;
    private Vehicle testVehicle1;
    private Vehicle testVehicle2;
    private User testDriver1;
    private User testDriver2;
    private Storage testStorage1;
    private Storage testStorage2;
    private Storage testStorage3;
    private Transportation testTransportationPlanned;
    private Transportation testTransportationInTransit;
    private Transportation testTransportationDelivered;
    private Transportation testTransportationCancelled;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        transportationRepository.deleteAll();
        itemRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
        storageRepository.deleteAll();
    }

    private void createTestData() {
        // Создаем тестовые предметы
        testItem1 = createItem("Laptop Dell XPS", ItemType.ELECTRONICS, ItemCondition.GOOD, "SN123456");
        testItem2 = createItem("Office Chair", ItemType.FURNITURE, ItemCondition.NEW, "SN789012");

        // Создаем тестовые транспортные средства
        testVehicle1 = createVehicle("ABC123", "Ford Transit", 2024, 1000, VehicleStatus.AVAILABLE);
        testVehicle2 = createVehicle("XYZ789", "Mercedes Sprinter", 2024, 1500, VehicleStatus.AVAILABLE);

        // Создаем тестовых водителей
        testDriver1 = createUser("driver1@example.com", "John", "Middle", "Driver", RoleType.DRIVER);
        testDriver2 = createUser("driver2@example.com", "Jane", "Middle", "Driver", RoleType.DRIVER);

        // Создаем тестовые склады
        testStorage1 = createStorage("Main Warehouse", "123 Main St", 1000);
        testStorage2 = createStorage("Secondary Storage", "456 Oak Ave", 500);
        testStorage3 = createStorage("Tertiary Storage", "789 Pine Rd", 300);

        // Создаем тестовые транспортировки
        testTransportationPlanned = createTransportation(
                testItem1, testVehicle1, testDriver1, testStorage1, testStorage2,
                TransportStatus.PLANNED,
                LocalDateTime.now().plusDays(1), null,
                LocalDateTime.now().plusDays(2), null,
                LocalDateTime.now().minusDays(1)
        );

        testTransportationInTransit = createTransportation(
                testItem2, testVehicle2, testDriver2, testStorage2, testStorage3,
                TransportStatus.IN_TRANSIT,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(2), null,
                LocalDateTime.now().minusDays(2)
        );

        testTransportationDelivered = createTransportation(
                testItem1, testVehicle1, testDriver1, testStorage3, testStorage1,
                TransportStatus.DELIVERED,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(4)
        );

        testTransportationCancelled = createTransportation(
                testItem2, testVehicle2, testDriver2, testStorage1, testStorage3,
                TransportStatus.CANCELLED,
                LocalDateTime.now().plusDays(1), null,
                LocalDateTime.now().plusDays(2), null,
                LocalDateTime.now().minusDays(1)
        );
    }

    private Item createItem(String name, ItemType type, ItemCondition condition, String serialNumber) {
        Item item = Item.builder()
                .name(name)
                .type(type)
                .condition(condition)
                .serialNumber(serialNumber)
                .description("Test " + name)
                .createdAt(LocalDateTime.now())
                .build();
        return itemRepository.save(item);
    }

    private Vehicle createVehicle(String licensePlate, String model, int year, int capacity, VehicleStatus status) {
        Vehicle vehicle = Vehicle.builder()
                .licensePlate(licensePlate)
                .brand("TestBrand")
                .model(model)
                .year(year)
                .capacity(capacity)
                .status(status)
                .build();
        return vehicleRepository.save(vehicle);
    }

    private User createUser(String email, String firstName, String secondName, String lastName, RoleType role) {
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .secondName(secondName)
                .lastName(lastName)
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    private Storage createStorage(String name, String address, Integer capacity) {
        Storage storage = Storage.builder()
                .name(name)
                .address(address)
                .capacity(capacity)
                .createdAt(LocalDateTime.now())
                .build();
        return storageRepository.save(storage);
    }

    private Transportation createTransportation(Item item, Vehicle vehicle, User driver,
                                                Storage fromStorage, Storage toStorage,
                                                TransportStatus status,
                                                LocalDateTime scheduledDeparture, LocalDateTime actualDeparture,
                                                LocalDateTime scheduledArrival, LocalDateTime actualArrival,
                                                LocalDateTime createdAt) {
        Transportation transportation = Transportation.builder()
                .item(item)
                .vehicle(vehicle)
                .driver(driver)
                .fromStorage(fromStorage)
                .toStorage(toStorage)
                .status(status)
                .scheduledDeparture(scheduledDeparture)
                .actualDeparture(actualDeparture)
                .scheduledArrival(scheduledArrival)
                .actualArrival(actualArrival)
                .createdAt(createdAt)
                .build();
        return transportationRepository.save(transportation);
    }

    @Test
    void create_ShouldCreateTransportation_WhenNoScheduledTimes() {
        Transportation newTransportation = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(null)
                .scheduledArrival(null)
                .createdAt(LocalDateTime.now())
                .build();

        Transportation result = transportationService.create(newTransportation);

        assertNotNull(result);
        assertEquals(TransportStatus.PLANNED, result.getStatus());
        assertNull(result.getScheduledDeparture());
        assertNull(result.getScheduledArrival());
    }

    @Test
    void create_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        // Создаем несуществующий предмет (не сохраняем в БД)
        Item nonExistentItem = Item.builder()
                .id(999L) // ID, которого нет в БД
                .name("Non-existent Item")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN999999")
                .description("Non-existent item")
                .createdAt(LocalDateTime.now())
                .build();

        Transportation newTransportation = Transportation.builder()
                .item(nonExistentItem)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .build();

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> transportationService.create(newTransportation)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentItem.getId()));
    }

    @Test
    void create_ShouldThrowUserNotFoundException_WhenDriverNotFound() {
        // Создаем несуществующего пользователя
        User nonExistentDriver = User.builder()
                .id(999L)
                .email("nonexistent@example.com")
                .firstName("Non")
                .lastName("Existent")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        Transportation newTransportation = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(nonExistentDriver)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .build();

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> transportationService.create(newTransportation)
        );

        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentDriver.getId()));
    }

    @Test
    void create_ShouldThrowOperationNotAllowedException_WhenSameFromAndToStorage() {
        Transportation newTransportation = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage1) // Тот же склад
                .status(TransportStatus.PLANNED)
                .build();

        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.create(newTransportation)
        );

        assertTrue(exception.getMessage().contains("From and to storage cannot be the same"));
    }

    @Test
    void getById_ShouldReturnTransportation_WhenTransportationExists() {
        Transportation result = transportationService.getById(testTransportationPlanned.getId());

        assertNotNull(result);
        assertEquals(testTransportationPlanned.getId(), result.getId());
        assertEquals(testItem1.getId(), result.getItem().getId());
        assertEquals(testVehicle1.getId(), result.getVehicle().getId());
        assertEquals(testDriver1.getId(), result.getDriver().getId());
        assertEquals(testStorage1.getId(), result.getFromStorage().getId());
        assertEquals(testStorage2.getId(), result.getToStorage().getId());
        assertEquals(TransportStatus.PLANNED, result.getStatus());
    }

    @Test
    void getById_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {
        Long nonExistentId = 999L;

        TransportationNotFoundException exception = assertThrows(
                TransportationNotFoundException.class,
                () -> transportationService.getById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Transportation not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldUpdateTransportation_WhenValidData() {
        Transportation updateTransportation = Transportation.builder()
                .item(testTransportationPlanned.getItem())
                .vehicle(testTransportationPlanned.getVehicle())
                .driver(testTransportationPlanned.getDriver())
                .fromStorage(testTransportationPlanned.getFromStorage())
                .toStorage(testTransportationPlanned.getToStorage())
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(3))
                .scheduledArrival(LocalDateTime.now().plusDays(4))
                .build();

        transportationService.update(testTransportationPlanned.getId(), updateTransportation);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(LocalDateTime.now().plusDays(3).withNano(0),
                updatedTransportation.getScheduledDeparture().withNano(0));
        assertEquals(LocalDateTime.now().plusDays(4).withNano(0),
                updatedTransportation.getScheduledArrival().withNano(0));
    }

    @Test
    void update_ShouldUpdateRelatedEntities_WhenEntitiesChanged() {
        Transportation updateTransportation = Transportation.builder()
                .item(testItem2) // Новый предмет
                .vehicle(testVehicle2) // Новый транспорт
                .driver(testDriver2) // Новый водитель
                .fromStorage(testStorage3) // Новый исходный склад
                .toStorage(testStorage1) // Новый целевой склад
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(testTransportationPlanned.getScheduledDeparture())
                .scheduledArrival(testTransportationPlanned.getScheduledArrival())
                .build();

        transportationService.update(testTransportationPlanned.getId(), updateTransportation);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(testItem2.getId(), updatedTransportation.getItem().getId());
        assertEquals(testVehicle2.getId(), updatedTransportation.getVehicle().getId());
        assertEquals(testDriver2.getId(), updatedTransportation.getDriver().getId());
        assertEquals(testStorage3.getId(), updatedTransportation.getFromStorage().getId());
        assertEquals(testStorage1.getId(), updatedTransportation.getToStorage().getId());
    }

    @Test
    void update_ShouldSetActualDeparture_WhenStatusChangedToInTransit() {
        Transportation updateTransportation = Transportation.builder()
                .item(testTransportationPlanned.getItem())
                .vehicle(testTransportationPlanned.getVehicle())
                .driver(testTransportationPlanned.getDriver())
                .fromStorage(testTransportationPlanned.getFromStorage())
                .toStorage(testTransportationPlanned.getToStorage())
                .status(TransportStatus.IN_TRANSIT) // Меняем статус
                .scheduledDeparture(testTransportationPlanned.getScheduledDeparture())
                .scheduledArrival(testTransportationPlanned.getScheduledArrival())
                .build();

        transportationService.update(testTransportationPlanned.getId(), updateTransportation);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(TransportStatus.IN_TRANSIT, updatedTransportation.getStatus());
        assertNotNull(updatedTransportation.getActualDeparture());
    }

    @Test
    void update_ShouldSetActualArrival_WhenStatusChangedToDelivered() {
        Transportation updateTransportation = Transportation.builder()
                .item(testTransportationInTransit.getItem())
                .vehicle(testTransportationInTransit.getVehicle())
                .driver(testTransportationInTransit.getDriver())
                .fromStorage(testTransportationInTransit.getFromStorage())
                .toStorage(testTransportationInTransit.getToStorage())
                .status(TransportStatus.DELIVERED) // Меняем статус
                .scheduledDeparture(testTransportationInTransit.getScheduledDeparture())
                .scheduledArrival(testTransportationInTransit.getScheduledArrival())
                .build();

        transportationService.update(testTransportationInTransit.getId(), updateTransportation);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationInTransit.getId()).orElseThrow();
        assertEquals(TransportStatus.DELIVERED, updatedTransportation.getStatus());
        assertNotNull(updatedTransportation.getActualArrival());
    }

    @Test
    void update_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {
        Long nonExistentId = 999L;
        Transportation updateTransportation = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .build();

        TransportationNotFoundException exception = assertThrows(
                TransportationNotFoundException.class,
                () -> transportationService.update(nonExistentId, updateTransportation)
        );

        assertTrue(exception.getMessage().contains("Transportation not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldThrowOperationNotAllowedException_WhenFinalStatus() {
        Transportation updateTransportation = Transportation.builder()
                .item(testTransportationDelivered.getItem())
                .vehicle(testTransportationDelivered.getVehicle())
                .driver(testTransportationDelivered.getDriver())
                .fromStorage(testTransportationDelivered.getFromStorage())
                .toStorage(testTransportationDelivered.getToStorage())
                .status(TransportStatus.DELIVERED)
                .build();

        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.update(testTransportationDelivered.getId(), updateTransportation)
        );

        assertTrue(exception.getMessage().contains("Cannot update transportation with status: DELIVERED"));
    }

    @Test
    void delete_ShouldDeleteTransportation_WhenTransportationExistsAndNotFinalStatus() {
        Long transportationId = testTransportationPlanned.getId();

        transportationService.delete(transportationId);

        assertFalse(transportationRepository.existsById(transportationId));
    }

    @Test
    void delete_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {
        Long nonExistentId = 999L;

        TransportationNotFoundException exception = assertThrows(
                TransportationNotFoundException.class,
                () -> transportationService.delete(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Transportation not found with ID: " + nonExistentId));
    }

    @Test
    void delete_ShouldThrowOperationNotAllowedException_WhenFinalStatus() {
        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.delete(testTransportationDelivered.getId())
        );

        assertTrue(exception.getMessage().contains("Cannot delete transportation with status: DELIVERED"));
    }

    @Test
    void findPage_ShouldReturnAllTransportations_WhenNoFilters() {
        Page<Transportation> result = transportationService.findPage(0, 10, null, null, null, null);

        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertEquals(4, result.getContent().size());
    }

    @Test
    void findPage_ShouldReturnFilteredByStatus_WhenStatusFilterApplied() {
        Page<Transportation> result = transportationService.findPage(0, 10, TransportStatus.PLANNED, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(TransportStatus.PLANNED, result.getContent().get(0).getStatus());
    }

    @Test
    void findPage_ShouldReturnFilteredByItem_WhenItemFilterApplied() {
        Page<Transportation> result = transportationService.findPage(0, 10, null, testItem1.getId(), null, null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getContent().stream().allMatch(t -> t.getItem().getId().equals(testItem1.getId())));
    }

    @Test
    void findPage_ShouldReturnFilteredByFromStorage_WhenFromStorageFilterApplied() {
        Page<Transportation> result = transportationService.findPage(0, 10, null, null, testStorage1.getId(), null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getContent().stream().allMatch(t -> t.getFromStorage().getId().equals(testStorage1.getId())));
    }

    @Test
    void findPage_ShouldReturnFilteredByToStorage_WhenToStorageFilterApplied() {
        Page<Transportation> result = transportationService.findPage(0, 10, null, null, null, testStorage2.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testStorage2.getId(), result.getContent().get(0).getToStorage().getId());
    }

    @Test
    void findPage_ShouldReturnFilteredByMultipleCriteria_WhenMultipleFiltersApplied() {
        Page<Transportation> result = transportationService.findPage(
                0, 10, TransportStatus.PLANNED, testItem1.getId(), testStorage1.getId(), testStorage2.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        Transportation transportation = result.getContent().get(0);
        assertEquals(TransportStatus.PLANNED, transportation.getStatus());
        assertEquals(testItem1.getId(), transportation.getItem().getId());
        assertEquals(testStorage1.getId(), transportation.getFromStorage().getId());
        assertEquals(testStorage2.getId(), transportation.getToStorage().getId());
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatchingFilters() {
        Page<Transportation> result = transportationService.findPage(0, 10, TransportStatus.PLANNED, 999L, 999L, 999L);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
        Page<Transportation> result = transportationService.findPage(0, 2, null, null, null, null);

        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void startTransportation_ShouldStartTransportation_WhenPlanned() {
        Transportation result = transportationService.startTransportation(testTransportationPlanned.getId());

        assertNotNull(result);
        assertEquals(TransportStatus.IN_TRANSIT, result.getStatus());
        assertNotNull(result.getActualDeparture());

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(TransportStatus.IN_TRANSIT, updatedTransportation.getStatus());
        assertNotNull(updatedTransportation.getActualDeparture());
    }

    @Test
    void startTransportation_ShouldThrowOperationNotAllowedException_WhenNotPlanned() {
        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.startTransportation(testTransportationInTransit.getId())
        );

        assertTrue(exception.getMessage().contains("Cannot start transportation with status: IN_TRANSIT"));
    }

    @Test
    void completeTransportation_ShouldCompleteTransportation_WhenInTransit() {
        Transportation result = transportationService.completeTransportation(testTransportationInTransit.getId());

        assertNotNull(result);
        assertEquals(TransportStatus.DELIVERED, result.getStatus());
        assertNotNull(result.getActualArrival());

        Transportation updatedTransportation = transportationRepository.findById(testTransportationInTransit.getId()).orElseThrow();
        assertEquals(TransportStatus.DELIVERED, updatedTransportation.getStatus());
        assertNotNull(updatedTransportation.getActualArrival());
    }

    @Test
    void completeTransportation_ShouldThrowOperationNotAllowedException_WhenNotInTransit() {
        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.completeTransportation(testTransportationPlanned.getId())
        );

        assertTrue(exception.getMessage().contains("Cannot complete transportation with status: PLANNED"));
    }

    @Test
    void cancelTransportation_ShouldCancelTransportation_WhenNotFinalStatus() {
        Transportation result = transportationService.cancelTransportation(testTransportationPlanned.getId());

        assertNotNull(result);
        assertEquals(TransportStatus.CANCELLED, result.getStatus());

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(TransportStatus.CANCELLED, updatedTransportation.getStatus());
    }

    @Test
    void cancelTransportation_ShouldThrowOperationNotAllowedException_WhenFinalStatus() {
        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.cancelTransportation(testTransportationDelivered.getId())
        );

        assertTrue(exception.getMessage().contains("Cannot cancel transportation with status: DELIVERED"));
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        long plannedCount = transportationService.countByStatus(TransportStatus.PLANNED);
        long inTransitCount = transportationService.countByStatus(TransportStatus.IN_TRANSIT);
        long deliveredCount = transportationService.countByStatus(TransportStatus.DELIVERED);
        long cancelledCount = transportationService.countByStatus(TransportStatus.CANCELLED);

        assertEquals(1, plannedCount);
        assertEquals(1, inTransitCount);
        assertEquals(1, deliveredCount);
        assertEquals(1, cancelledCount);
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
        Page<Transportation> result = transportationService.findPage(10, 10, null, null, null, null);

        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void update_ShouldNotSetActualTimes_WhenTheyAreAlreadySet() {
        LocalDateTime existingActualDeparture = testTransportationInTransit.getActualDeparture();
        LocalDateTime existingActualArrival = testTransportationInTransit.getActualArrival();

        Transportation updateTransportation = Transportation.builder()
                .item(testTransportationInTransit.getItem())
                .vehicle(testTransportationInTransit.getVehicle())
                .driver(testTransportationInTransit.getDriver())
                .fromStorage(testTransportationInTransit.getFromStorage())
                .toStorage(testTransportationInTransit.getToStorage())
                .status(TransportStatus.IN_TRANSIT)
                .scheduledDeparture(testTransportationInTransit.getScheduledDeparture())
                .scheduledArrival(testTransportationInTransit.getScheduledArrival())
                .build();

        transportationService.update(testTransportationInTransit.getId(), updateTransportation);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationInTransit.getId()).orElseThrow();
        assertEquals(existingActualDeparture, updatedTransportation.getActualDeparture());
        assertEquals(existingActualArrival, updatedTransportation.getActualArrival());
    }
}