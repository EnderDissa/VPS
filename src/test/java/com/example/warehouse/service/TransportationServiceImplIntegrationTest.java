package com.example.warehouse.service;

import com.example.warehouse.dto.TransportationDTO;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
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
        transportationRepository.deleteAll();
        itemRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
        storageRepository.deleteAll();

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

        testVehicle1 = Vehicle.builder()
                .licensePlate("ABC123")
                .brand("ford")
                .model("Ford Transit")
                .year(2024)
                .capacity(1000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        testVehicle2 = Vehicle.builder()
                .licensePlate("XYZ789")
                .brand("ford")
                .model("Mercedes Sprinter")
                .year(2024)
                .capacity(1500)
                .status(VehicleStatus.AVAILABLE)
                .build();

        testVehicle1 = vehicleRepository.save(testVehicle1);
        testVehicle2 = vehicleRepository.save(testVehicle2);

        testDriver1 = User.builder()
                .email("driver1@example.com")
                .firstName("John")
                .secondName("Middle")
                .lastName("Driver")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        testDriver2 = User.builder()
                .email("driver2@example.com")
                .firstName("Jane")
                .secondName("Middle")
                .lastName("Driver")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        testDriver1 = userRepository.save(testDriver1);
        testDriver2 = userRepository.save(testDriver2);

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

        testStorage3 = Storage.builder()
                .name("Tertiary Storage")
                .address("789 Pine Rd")
                .capacity(300)
                .createdAt(LocalDateTime.now())
                .build();

        testStorage1 = storageRepository.save(testStorage1);
        testStorage2 = storageRepository.save(testStorage2);
        testStorage3 = storageRepository.save(testStorage3);

        testTransportationPlanned = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        testTransportationInTransit = Transportation.builder()
                .item(testItem2)
                .vehicle(testVehicle2)
                .driver(testDriver2)
                .fromStorage(testStorage2)
                .toStorage(testStorage3)
                .status(TransportStatus.IN_TRANSIT)
                .scheduledDeparture(LocalDateTime.now().minusHours(2))
                .actualDeparture(LocalDateTime.now().minusHours(1))
                .scheduledArrival(LocalDateTime.now().plusHours(2))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        testTransportationDelivered = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage3)
                .toStorage(testStorage1)
                .status(TransportStatus.DELIVERED)
                .scheduledDeparture(LocalDateTime.now().minusDays(3))
                .actualDeparture(LocalDateTime.now().minusDays(3))
                .scheduledArrival(LocalDateTime.now().minusDays(2))
                .actualArrival(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(4))
                .build();

        testTransportationCancelled = Transportation.builder()
                .item(testItem2)
                .vehicle(testVehicle2)
                .driver(testDriver2)
                .fromStorage(testStorage1)
                .toStorage(testStorage3)
                .status(TransportStatus.CANCELLED)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        testTransportationPlanned = transportationRepository.save(testTransportationPlanned);
        testTransportationInTransit = transportationRepository.save(testTransportationInTransit);
        testTransportationDelivered = transportationRepository.save(testTransportationDelivered);
        testTransportationCancelled = transportationRepository.save(testTransportationCancelled);
    }

    @Test
    void create_ShouldCreateTransportation_WhenNoScheduledTimes() {
        TransportationDTO newTransportationDTO = new TransportationDTO(
                null,
                testItem1.getId(),
                testVehicle1.getId(),
                testDriver1.getId(),
                testStorage1.getId(),
                testStorage2.getId(),
                TransportStatus.PLANNED,
                null,
                null,
                null,
                null,
                null
        );

        TransportationDTO result = transportationService.create(newTransportationDTO);

        assertNotNull(result);
        assertEquals(TransportStatus.PLANNED, result.status());
        assertNull(result.scheduledDeparture());
        assertNull(result.scheduledArrival());
    }

    @Test
    void create_ShouldThrowItemNotFoundException_WhenItemNotFound() {
        Long nonExistentItemId = 999L;
        TransportationDTO newTransportationDTO = new TransportationDTO(
                null,
                nonExistentItemId,
                testVehicle1.getId(),
                testDriver1.getId(),
                testStorage1.getId(),
                testStorage2.getId(),
                TransportStatus.PLANNED,
                null, null, null, null, null
        );

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> transportationService.create(newTransportationDTO)
        );

        assertTrue(exception.getMessage().contains("Item not found with ID: " + nonExistentItemId));
    }

    @Test
    void create_ShouldThrowVehicleNotFoundException_WhenVehicleNotFound() {
        Long nonExistentVehicleId = 999L;
        TransportationDTO newTransportationDTO = new TransportationDTO(
                null,
                testItem1.getId(),
                nonExistentVehicleId,
                testDriver1.getId(),
                testStorage1.getId(),
                testStorage2.getId(),
                TransportStatus.PLANNED,
                null, null, null, null, null
        );

        VehicleNotFoundException exception = assertThrows(
                VehicleNotFoundException.class,
                () -> transportationService.create(newTransportationDTO)
        );

        assertTrue(exception.getMessage().contains("Vehicle not found with ID: " + nonExistentVehicleId));
    }

    @Test
    void create_ShouldThrowUserNotFoundException_WhenDriverNotFound() {
        Long nonExistentDriverId = 999L;
        TransportationDTO newTransportationDTO = new TransportationDTO(
                null,
                testItem1.getId(),
                testVehicle1.getId(),
                nonExistentDriverId,
                testStorage1.getId(),
                testStorage2.getId(),
                TransportStatus.PLANNED,
                null, null, null, null, null
        );

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> transportationService.create(newTransportationDTO)
        );

        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentDriverId));
    }

    @Test
    void create_ShouldThrowStorageNotFoundException_WhenFromStorageNotFound() {
        Long nonExistentStorageId = 999L;
        TransportationDTO newTransportationDTO = new TransportationDTO(
                null,
                testItem1.getId(),
                testVehicle1.getId(),
                testDriver1.getId(),
                nonExistentStorageId,
                testStorage2.getId(),
                TransportStatus.PLANNED,
                null, null, null, null, null
        );

        StorageNotFoundException exception = assertThrows(
                StorageNotFoundException.class,
                () -> transportationService.create(newTransportationDTO)
        );

        assertTrue(exception.getMessage().contains("From storage not found with ID: " + nonExistentStorageId));
    }

    @Test
    void create_ShouldThrowStorageNotFoundException_WhenToStorageNotFound() {
        Long nonExistentStorageId = 999L;
        TransportationDTO newTransportationDTO = new TransportationDTO(
                null,
                testItem1.getId(),
                testVehicle1.getId(),
                testDriver1.getId(),
                testStorage1.getId(),
                nonExistentStorageId,
                TransportStatus.PLANNED,
                null, null, null, null, null
        );

        StorageNotFoundException exception = assertThrows(
                StorageNotFoundException.class,
                () -> transportationService.create(newTransportationDTO)
        );

        assertTrue(exception.getMessage().contains("To storage not found with ID: " + nonExistentStorageId));
    }

    @Test
    void create_ShouldThrowOperationNotAllowedException_WhenSameFromAndToStorage() {
        TransportationDTO newTransportationDTO = new TransportationDTO(
                null,
                testItem1.getId(),
                testVehicle1.getId(),
                testDriver1.getId(),
                testStorage1.getId(),
                testStorage1.getId(),
                TransportStatus.PLANNED,
                null, null, null, null, null
        );

        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.create(newTransportationDTO)
        );

        assertTrue(exception.getMessage().contains("From and to storage cannot be the same"));
    }

    @Test
    void getById_ShouldReturnTransportation_WhenTransportationExists() {
        TransportationDTO result = transportationService.getById(testTransportationPlanned.getId());

        assertNotNull(result);
        assertEquals(testTransportationPlanned.getId(), result.id());
        assertEquals(testItem1.getId(), result.itemId());
        assertEquals(testVehicle1.getId(), result.vehicleId());
        assertEquals(testDriver1.getId(), result.driverId());
        assertEquals(testStorage1.getId(), result.fromStorageId());
        assertEquals(testStorage2.getId(), result.toStorageId());
        assertEquals(TransportStatus.PLANNED, result.status());
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
        TransportationDTO updateDTO = new TransportationDTO(
                testTransportationPlanned.getId(),
                testTransportationPlanned.getItem().getId(),
                testTransportationPlanned.getVehicle().getId(),
                testTransportationPlanned.getDriver().getId(),
                testTransportationPlanned.getFromStorage().getId(),
                testTransportationPlanned.getToStorage().getId(),
                TransportStatus.PLANNED,
                LocalDateTime.now().plusDays(3),
                null,
                LocalDateTime.now().plusDays(4),
                null,
                null
        );

        transportationService.update(testTransportationPlanned.getId(), updateDTO);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(LocalDateTime.now().plusDays(3).withNano(0),
                updatedTransportation.getScheduledDeparture().withNano(0));
        assertEquals(LocalDateTime.now().plusDays(4).withNano(0),
                updatedTransportation.getScheduledArrival().withNano(0));
    }

    @Test
    void update_ShouldUpdateRelatedEntities_WhenEntitiesChanged() {
        TransportationDTO updateDTO = new TransportationDTO(
                testTransportationPlanned.getId(),
                testItem2.getId(),
                testVehicle2.getId(),
                testDriver2.getId(),
                testStorage3.getId(),
                testStorage1.getId(),
                TransportStatus.PLANNED,
                testTransportationPlanned.getScheduledDeparture(),
                null,
                testTransportationPlanned.getScheduledArrival(),
                null,
                null
        );

        transportationService.update(testTransportationPlanned.getId(), updateDTO);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(testItem2.getId(), updatedTransportation.getItem().getId());
        assertEquals(testVehicle2.getId(), updatedTransportation.getVehicle().getId());
        assertEquals(testDriver2.getId(), updatedTransportation.getDriver().getId());
        assertEquals(testStorage3.getId(), updatedTransportation.getFromStorage().getId());
        assertEquals(testStorage1.getId(), updatedTransportation.getToStorage().getId());
    }

    @Test
    void update_ShouldSetActualDeparture_WhenStatusChangedToInTransit() {
        TransportationDTO updateDTO = new TransportationDTO(
                testTransportationPlanned.getId(),
                testTransportationPlanned.getItem().getId(),
                testTransportationPlanned.getVehicle().getId(),
                testTransportationPlanned.getDriver().getId(),
                testTransportationPlanned.getFromStorage().getId(),
                testTransportationPlanned.getToStorage().getId(),
                TransportStatus.IN_TRANSIT,
                testTransportationPlanned.getScheduledDeparture(),
                null,
                testTransportationPlanned.getScheduledArrival(),
                null,
                null
        );

        transportationService.update(testTransportationPlanned.getId(), updateDTO);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationPlanned.getId()).orElseThrow();
        assertEquals(TransportStatus.IN_TRANSIT, updatedTransportation.getStatus());
        assertNotNull(updatedTransportation.getActualDeparture());
    }

    @Test
    void update_ShouldSetActualArrival_WhenStatusChangedToDelivered() {
        TransportationDTO updateDTO = new TransportationDTO(
                testTransportationInTransit.getId(),
                testTransportationInTransit.getItem().getId(),
                testTransportationInTransit.getVehicle().getId(),
                testTransportationInTransit.getDriver().getId(),
                testTransportationInTransit.getFromStorage().getId(),
                testTransportationInTransit.getToStorage().getId(),
                TransportStatus.DELIVERED,
                testTransportationInTransit.getScheduledDeparture(),
                null,
                testTransportationInTransit.getScheduledArrival(),
                null,
                null
        );

        transportationService.update(testTransportationInTransit.getId(), updateDTO);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationInTransit.getId()).orElseThrow();
        assertEquals(TransportStatus.DELIVERED, updatedTransportation.getStatus());
        assertNotNull(updatedTransportation.getActualArrival());
    }

    @Test
    void update_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {
        Long nonExistentId = 999L;
        TransportationDTO updateDTO = new TransportationDTO(
                nonExistentId,
                testItem1.getId(),
                testVehicle1.getId(),
                testDriver1.getId(),
                testStorage1.getId(),
                testStorage2.getId(),
                TransportStatus.PLANNED,
                null, null, null, null, null
        );

        TransportationNotFoundException exception = assertThrows(
                TransportationNotFoundException.class,
                () -> transportationService.update(nonExistentId, updateDTO)
        );

        assertTrue(exception.getMessage().contains("Transportation not found with ID: " + nonExistentId));
    }

    @Test
    void update_ShouldThrowOperationNotAllowedException_WhenFinalStatus() {
        TransportationDTO updateDTO = new TransportationDTO(
                testTransportationDelivered.getId(),
                testTransportationDelivered.getItem().getId(),
                testTransportationDelivered.getVehicle().getId(),
                testTransportationDelivered.getDriver().getId(),
                testTransportationDelivered.getFromStorage().getId(),
                testTransportationDelivered.getToStorage().getId(),
                TransportStatus.DELIVERED,
                null, null, null, null, null
        );

        OperationNotAllowedException exception = assertThrows(
                OperationNotAllowedException.class,
                () -> transportationService.update(testTransportationDelivered.getId(), updateDTO)
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
        Page<TransportationDTO> result = transportationService.findPage(0, 10, null, null, null, null);

        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertEquals(4, result.getContent().size());
    }

    @Test
    void findPage_ShouldReturnFilteredByStatus_WhenStatusFilterApplied() {
        Page<TransportationDTO> result = transportationService.findPage(0, 10, TransportStatus.PLANNED, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(TransportStatus.PLANNED, result.getContent().get(0).status());
    }

    @Test
    void findPage_ShouldReturnFilteredByItem_WhenItemFilterApplied() {
        Page<TransportationDTO> result = transportationService.findPage(0, 10, null, testItem1.getId(), null, null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getContent().stream().allMatch(t -> t.itemId().equals(testItem1.getId())));
    }

    @Test
    void findPage_ShouldReturnFilteredByFromStorage_WhenFromStorageFilterApplied() {
        Page<TransportationDTO> result = transportationService.findPage(0, 10, null, null, testStorage1.getId(), null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getContent().stream().allMatch(t -> t.fromStorageId().equals(testStorage1.getId())));
    }

    @Test
    void findPage_ShouldReturnFilteredByToStorage_WhenToStorageFilterApplied() {
        Page<TransportationDTO> result = transportationService.findPage(0, 10, null, null, null, testStorage2.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testStorage2.getId(), result.getContent().get(0).toStorageId());
    }

    @Test
    void findPage_ShouldReturnFilteredByMultipleCriteria_WhenMultipleFiltersApplied() {
        Page<TransportationDTO> result = transportationService.findPage(
                0, 10, TransportStatus.PLANNED, testItem1.getId(), testStorage1.getId(), testStorage2.getId());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        TransportationDTO dto = result.getContent().get(0);
        assertEquals(TransportStatus.PLANNED, dto.status());
        assertEquals(testItem1.getId(), dto.itemId());
        assertEquals(testStorage1.getId(), dto.fromStorageId());
        assertEquals(testStorage2.getId(), dto.toStorageId());
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatchingFilters() {
        Page<TransportationDTO> result = transportationService.findPage(0, 10, TransportStatus.PLANNED, 999L, 999L, 999L);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
        Page<TransportationDTO> result = transportationService.findPage(0, 2, null, null, null, null);

        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void startTransportation_ShouldStartTransportation_WhenPlanned() {
        TransportationDTO result = transportationService.startTransportation(testTransportationPlanned.getId());

        assertNotNull(result);
        assertEquals(TransportStatus.IN_TRANSIT, result.status());
        assertNotNull(result.actualDeparture());

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
        TransportationDTO result = transportationService.completeTransportation(testTransportationInTransit.getId());

        assertNotNull(result);
        assertEquals(TransportStatus.DELIVERED, result.status());
        assertNotNull(result.actualArrival());

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
        TransportationDTO result = transportationService.cancelTransportation(testTransportationPlanned.getId());

        assertNotNull(result);
        assertEquals(TransportStatus.CANCELLED, result.status());

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
        Page<TransportationDTO> result = transportationService.findPage(10, 10, null, null, null, null);

        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void update_ShouldNotSetActualTimes_WhenTheyAreAlreadySet() {
        LocalDateTime existingActualDeparture = testTransportationInTransit.getActualDeparture();
        LocalDateTime existingActualArrival = testTransportationInTransit.getActualArrival();

        TransportationDTO updateDTO = new TransportationDTO(
                testTransportationInTransit.getId(),
                testTransportationInTransit.getItem().getId(),
                testTransportationInTransit.getVehicle().getId(),
                testTransportationInTransit.getDriver().getId(),
                testTransportationInTransit.getFromStorage().getId(),
                testTransportationInTransit.getToStorage().getId(),
                TransportStatus.IN_TRANSIT,
                testTransportationInTransit.getScheduledDeparture(),
                null,
                testTransportationInTransit.getScheduledArrival(),
                null,
                null
        );

        transportationService.update(testTransportationInTransit.getId(), updateDTO);

        Transportation updatedTransportation = transportationRepository.findById(testTransportationInTransit.getId()).orElseThrow();
        assertEquals(existingActualDeparture, updatedTransportation.getActualDeparture());
        assertEquals(existingActualArrival, updatedTransportation.getActualArrival());
    }
}