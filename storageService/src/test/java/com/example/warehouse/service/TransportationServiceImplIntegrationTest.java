package com.example.warehouse.service;

import com.example.warehouse.client.ItemServiceClient;
import com.example.warehouse.client.UserServiceClient;
import com.example.warehouse.entity.*;
import com.example.warehouse.enumeration.*;
import com.example.warehouse.exception.*;
import com.example.warehouse.repository.TransportationRepository;
import com.example.warehouse.service.interfaces.StorageService;
import com.example.warehouse.service.interfaces.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportationServiceImplTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private ItemServiceClient itemServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private TransportationServiceImpl transportationService;

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

    @BeforeEach
    void setUp() {
        // Создаем тестовые предметы
        testItem1 = Item.builder()
                .id(1L)
                .name("Laptop Dell XPS")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .description("Test laptop")
                .createdAt(LocalDateTime.now())
                .build();

        testItem2 = Item.builder()
                .id(2L)
                .name("Office Chair")
                .type(ItemType.FURNITURE)
                .condition(ItemCondition.NEW)
                .serialNumber("SN789012")
                .description("Test chair")
                .createdAt(LocalDateTime.now())
                .build();

        // Создаем тестовые транспортные средства
        testVehicle1 = Vehicle.builder()
                .id(1L)
                .licensePlate("ABC123")
                .brand("Ford")
                .model("Transit")
                .year(2024)
                .capacity(1000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        testVehicle2 = Vehicle.builder()
                .id(2L)
                .licensePlate("XYZ789")
                .brand("Mercedes")
                .model("Sprinter")
                .year(2024)
                .capacity(1500)
                .status(VehicleStatus.AVAILABLE)
                .build();

        // Создаем тестовых водителей
        testDriver1 = User.builder()
                .id(1L)
                .email("driver1@example.com")
                .firstName("John")
                .secondName("Middle")
                .lastName("Driver")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        testDriver2 = User.builder()
                .id(2L)
                .email("driver2@example.com")
                .firstName("Jane")
                .secondName("Middle")
                .lastName("Driver")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        // Создаем тестовые склады
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

        testStorage3 = Storage.builder()
                .id(3L)
                .name("Tertiary Storage")
                .address("789 Pine Rd")
                .capacity(300)
                .createdAt(LocalDateTime.now())
                .build();

        // Создаем тестовые транспортировки
        testTransportationPlanned = Transportation.builder()
                .id(1L)
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .actualDeparture(null)
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .actualArrival(null)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        testTransportationInTransit = Transportation.builder()
                .id(2L)
                .item(testItem2)
                .vehicle(testVehicle2)
                .driver(testDriver2)
                .fromStorage(testStorage2)
                .toStorage(testStorage3)
                .status(TransportStatus.IN_TRANSIT)
                .scheduledDeparture(LocalDateTime.now().minusHours(2))
                .actualDeparture(LocalDateTime.now().minusHours(1))
                .scheduledArrival(LocalDateTime.now().plusHours(2))
                .actualArrival(null)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        testTransportationDelivered = Transportation.builder()
                .id(3L)
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
    }

    @Test
    void create_ShouldCreateTransportation_WhenValidData() {
        // Arrange
        Transportation newTransportation = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .build();

        Transportation savedTransportation = Transportation.builder()
                .id(4L)
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .build();

        when(itemServiceClient.getItemById(1L)).thenReturn(Mono.just(testItem1));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(testDriver1));
        when(vehicleService.getById(1L)).thenReturn(Mono.just(testVehicle1));
        when(storageService.getById(1L)).thenReturn(Mono.just(testStorage1));
        when(storageService.getById(2L)).thenReturn(Mono.just(testStorage2));
        when(transportationRepository.save(newTransportation)).thenReturn(savedTransportation);

        // Act & Assert
        StepVerifier.create(transportationService.create(newTransportation))
                .expectNextMatches(transportation -> {
                    assertEquals(4L, transportation.getId());
                    assertEquals(TransportStatus.PLANNED, transportation.getStatus());
                    assertEquals(testItem1.getId(), transportation.getItem().getId());
                    assertEquals(testVehicle1.getId(), transportation.getVehicle().getId());
                    assertEquals(testDriver1.getId(), transportation.getDriver().getId());
                    assertEquals(testStorage1.getId(), transportation.getFromStorage().getId());
                    assertEquals(testStorage2.getId(), transportation.getToStorage().getId());
                    return true;
                })
                .verifyComplete();

        verify(itemServiceClient).getItemById(1L);
        verify(userServiceClient).getUserById(1L);
        verify(vehicleService).getById(1L);
        verify(storageService).getById(1L);
        verify(storageService).getById(2L);
        verify(transportationRepository).save(newTransportation);
    }

    @Test
    void create_ShouldThrowOperationNotAllowedException_WhenSameFromAndToStorage() {
        // Arrange
        Transportation newTransportation = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage1) // Тот же склад
                .status(TransportStatus.PLANNED)
                .build();

        // Act & Assert
        StepVerifier.create(transportationService.create(newTransportation))
                .expectErrorMatches(throwable ->
                        throwable instanceof OperationNotAllowedException &&
                                throwable.getMessage().contains("From and to storage cannot be the same")
                )
                .verify();

        verify(itemServiceClient, never()).getItemById(anyLong());
        verify(userServiceClient, never()).getUserById(anyLong());
        verify(vehicleService, never()).getById(anyLong());
        verify(storageService, never()).getById(anyLong());
        verify(transportationRepository, never()).save(any());
    }



    @Test
    void getById_ShouldReturnTransportation_WhenTransportationExists() {
        // Arrange
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));

        // Act & Assert
        StepVerifier.create(transportationService.getById(1L))
                .expectNextMatches(transportation -> {
                    assertEquals(testTransportationPlanned.getId(), transportation.getId());
                    assertEquals(testItem1.getId(), transportation.getItem().getId());
                    assertEquals(testVehicle1.getId(), transportation.getVehicle().getId());
                    assertEquals(testDriver1.getId(), transportation.getDriver().getId());
                    assertEquals(testStorage1.getId(), transportation.getFromStorage().getId());
                    assertEquals(testStorage2.getId(), transportation.getToStorage().getId());
                    assertEquals(TransportStatus.PLANNED, transportation.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {
        // Arrange
        when(transportationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(transportationService.getById(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof TransportationNotFoundException &&
                                throwable.getMessage().contains("Transportation not found with ID: 999")
                )
                .verify();

        verify(transportationRepository).findById(999L);
    }

    @Test
    void update_ShouldUpdateTransportation_WhenValidData() {
        // Arrange
        Transportation updateData = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(3))
                .scheduledArrival(LocalDateTime.now().plusDays(4))
                .build();

        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(transportationService.update(1L, updateData))
                .expectNextMatches(transportation -> {
                    assertNotNull(transportation.getScheduledDeparture());
                    assertNotNull(transportation.getScheduledArrival());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findById(1L);
        verify(transportationRepository).save(any(Transportation.class));
        verify(itemServiceClient, never()).getItemById(anyLong());
        verify(userServiceClient, never()).getUserById(anyLong());
        verify(vehicleService, never()).getById(anyLong());
        verify(storageService, never()).getById(anyLong());
    }

    @Test
    void update_ShouldUpdateRelatedEntities_WhenEntitiesChanged() {
        // Arrange
        Transportation updateData = Transportation.builder()
                .item(testItem2)
                .vehicle(testVehicle2)
                .driver(testDriver2)
                .fromStorage(testStorage3)
                .toStorage(testStorage1)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .build();

        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));
        when(itemServiceClient.getItemById(2L)).thenReturn(Mono.just(testItem2));
        when(userServiceClient.getUserById(2L)).thenReturn(Mono.just(testDriver2));
        when(vehicleService.getById(2L)).thenReturn(Mono.just(testVehicle2));
        when(storageService.getById(3L)).thenReturn(Mono.just(testStorage3));
        when(storageService.getById(1L)).thenReturn(Mono.just(testStorage1));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(transportationService.update(1L, updateData))
                .expectNextMatches(transportation -> {
                    assertEquals(testItem2.getId(), transportation.getItem().getId());
                    assertEquals(testVehicle2.getId(), transportation.getVehicle().getId());
                    assertEquals(testDriver2.getId(), transportation.getDriver().getId());
                    assertEquals(testStorage3.getId(), transportation.getFromStorage().getId());
                    assertEquals(testStorage1.getId(), transportation.getToStorage().getId());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findById(1L);
        verify(itemServiceClient).getItemById(2L);
        verify(userServiceClient).getUserById(2L);
        verify(vehicleService).getById(2L);
        verify(storageService).getById(3L);
        verify(storageService).getById(1L);
        verify(transportationRepository).save(any(Transportation.class));
    }

    @Test
    void update_ShouldSetActualDeparture_WhenStatusChangedToInTransit() {
        // Arrange
        Transportation updateData = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.IN_TRANSIT)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .build();

        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(transportationService.update(1L, updateData))
                .expectNextMatches(transportation -> {
                    assertEquals(TransportStatus.IN_TRANSIT, transportation.getStatus());
                    assertNotNull(transportation.getActualDeparture());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findById(1L);
        verify(transportationRepository).save(any(Transportation.class));
    }

    @Test
    void update_ShouldSetActualArrival_WhenStatusChangedToDelivered() {
        // Arrange
        Transportation updateData = Transportation.builder()
                .item(testItem2)
                .vehicle(testVehicle2)
                .driver(testDriver2)
                .fromStorage(testStorage2)
                .toStorage(testStorage3)
                .status(TransportStatus.DELIVERED)
                .scheduledDeparture(LocalDateTime.now().minusHours(2))
                .scheduledArrival(LocalDateTime.now().plusHours(2))
                .build();

        when(transportationRepository.findById(2L)).thenReturn(Optional.of(testTransportationInTransit));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(transportationService.update(2L, updateData))
                .expectNextMatches(transportation -> {
                    assertEquals(TransportStatus.DELIVERED, transportation.getStatus());
                    assertNotNull(transportation.getActualArrival());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findById(2L);
        verify(transportationRepository).save(any(Transportation.class));
    }

    @Test
    void update_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {
        // Arrange
        Transportation updateData = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .build();

        when(transportationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(transportationService.update(999L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof TransportationNotFoundException &&
                                throwable.getMessage().contains("Transportation not found with ID: 999")
                )
                .verify();

        verify(transportationRepository).findById(999L);
        verify(transportationRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowOperationNotAllowedException_WhenFinalStatus() {
        // Arrange
        Transportation updateData = Transportation.builder()
                .item(testItem1)
                .vehicle(testVehicle1)
                .driver(testDriver1)
                .fromStorage(testStorage3)
                .toStorage(testStorage1)
                .status(TransportStatus.DELIVERED)
                .build();

        when(transportationRepository.findById(3L)).thenReturn(Optional.of(testTransportationDelivered));

        // Act & Assert
        StepVerifier.create(transportationService.update(3L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof OperationNotAllowedException &&
                                throwable.getMessage().contains("Cannot update transportation with status: DELIVERED")
                )
                .verify();

        verify(transportationRepository).findById(3L);
        verify(transportationRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteTransportation_WhenTransportationExistsAndNotFinalStatus() {
        // Arrange
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));
        doNothing().when(transportationRepository).deleteById(1L);

        // Act & Assert
        StepVerifier.create(transportationService.delete(1L))
                .verifyComplete();

        verify(transportationRepository).findById(1L);
        verify(transportationRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {
        // Arrange
        when(transportationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(transportationService.delete(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof TransportationNotFoundException &&
                                throwable.getMessage().contains("Transportation not found with ID: 999")
                )
                .verify();

        verify(transportationRepository).findById(999L);
        verify(transportationRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_ShouldThrowOperationNotAllowedException_WhenFinalStatus() {
        // Arrange
        when(transportationRepository.findById(3L)).thenReturn(Optional.of(testTransportationDelivered));

        // Act & Assert
        StepVerifier.create(transportationService.delete(3L))
                .expectErrorMatches(throwable ->
                        throwable instanceof OperationNotAllowedException &&
                                throwable.getMessage().contains("Cannot delete transportation with status: DELIVERED")
                )
                .verify();

        verify(transportationRepository).findById(3L);
        verify(transportationRepository, never()).deleteById(anyLong());
    }

    @Test
    void findPage_ShouldReturnAllTransportations_WhenNoFilters() {
        // Arrange
        List<Transportation> transportations = List.of(testTransportationPlanned, testTransportationInTransit, testTransportationDelivered);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(transportationService.findPage(0, 10, null, null, null, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertEquals(3, result.getContent().size());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findAll(pageable);
    }

    @Test
    void findPage_ShouldReturnFilteredByStatus_WhenStatusFilterApplied() {
        // Arrange
        List<Transportation> transportations = List.of(testTransportationPlanned);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findByStatus(TransportStatus.PLANNED, pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(transportationService.findPage(0, 10, TransportStatus.PLANNED, null, null, null))
                .expectNextMatches(result -> {
                    assertEquals(1, result.getTotalElements());
                    assertEquals(TransportStatus.PLANNED, result.getContent().get(0).getStatus());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findByStatus(TransportStatus.PLANNED, pageable);
    }

    @Test
    void findPage_ShouldReturnFilteredByItem_WhenItemFilterApplied() {
        // Arrange
        List<Transportation> transportations = List.of(testTransportationPlanned, testTransportationDelivered);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findByItemId(1L, pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(transportationService.findPage(0, 10, null, 1L, null, null))
                .expectNextMatches(result -> {
                    assertEquals(2, result.getTotalElements());
                    assertTrue(result.getContent().stream().allMatch(t -> t.getItem().getId().equals(1L)));
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findByItemId(1L, pageable);
    }

    @Test
    void findPage_ShouldReturnFilteredByMultipleCriteria_WhenMultipleFiltersApplied() {
        // Arrange
        List<Transportation> transportations = List.of(testTransportationPlanned);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findByStatusAndItemIdAndFromStorageIdAndToStorageId(
                TransportStatus.PLANNED, 1L, 1L, 2L, pageable))
                .thenReturn(page);

        // Act & Assert
        StepVerifier.create(transportationService.findPage(0, 10, TransportStatus.PLANNED, 1L, 1L, 2L))
                .expectNextMatches(result -> {
                    assertEquals(1, result.getTotalElements());
                    Transportation transportation = result.getContent().get(0);
                    assertEquals(TransportStatus.PLANNED, transportation.getStatus());
                    assertEquals(1L, transportation.getItem().getId());
                    assertEquals(1L, transportation.getFromStorage().getId());
                    assertEquals(2L, transportation.getToStorage().getId());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findByStatusAndItemIdAndFromStorageIdAndToStorageId(
                TransportStatus.PLANNED, 1L, 1L, 2L, pageable);
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatchingFilters() {
        // Arrange
        List<Transportation> emptyList = List.of();
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(emptyList, pageable, 0);

        when(transportationRepository.findByStatus(TransportStatus.PLANNED, pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(transportationService.findPage(0, 10, TransportStatus.PLANNED, 999L, 999L, 999L))
                .expectNextMatches(result -> {
                    assertEquals(0, result.getTotalElements());
                    assertTrue(result.getContent().isEmpty());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findByStatus(TransportStatus.PLANNED, pageable);
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {
        // Arrange
        List<Transportation> firstPage = List.of(testTransportationPlanned, testTransportationInTransit);
        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(firstPage, pageable, 3);

        when(transportationRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(transportationService.findPage(0, 2, null, null, null, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertEquals(2, result.getContent().size());
                    assertEquals(2, result.getTotalPages());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenPageOutOfRange() {
        // Arrange
        List<Transportation> emptyList = List.of();
        PageRequest pageable = PageRequest.of(10, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(emptyList, pageable, 3);

        when(transportationRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(transportationService.findPage(10, 10, null, null, null, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertTrue(result.getContent().isEmpty());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void create_ShouldCreateTransportation_WhenNoScheduledTimes() {
        // Arrange
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

        Transportation savedTransportation = Transportation.builder()
                .id(5L)
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

        when(itemServiceClient.getItemById(1L)).thenReturn(Mono.just(testItem1));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(testDriver1));
        when(vehicleService.getById(1L)).thenReturn(Mono.just(testVehicle1));
        when(storageService.getById(1L)).thenReturn(Mono.just(testStorage1));
        when(storageService.getById(2L)).thenReturn(Mono.just(testStorage2));
        when(transportationRepository.save(newTransportation)).thenReturn(savedTransportation);

        // Act & Assert
        StepVerifier.create(transportationService.create(newTransportation))
                .expectNextMatches(transportation -> {
                    assertEquals(TransportStatus.PLANNED, transportation.getStatus());
                    assertNull(transportation.getScheduledDeparture());
                    assertNull(transportation.getScheduledArrival());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void update_ShouldNotSetActualTimes_WhenTheyAreAlreadySet() {
        // Arrange
        LocalDateTime existingActualDeparture = testTransportationInTransit.getActualDeparture();

        Transportation updateData = Transportation.builder()
                .item(testItem2)
                .vehicle(testVehicle2)
                .driver(testDriver2)
                .fromStorage(testStorage2)
                .toStorage(testStorage3)
                .status(TransportStatus.IN_TRANSIT)
                .scheduledDeparture(LocalDateTime.now().minusHours(2))
                .scheduledArrival(LocalDateTime.now().plusHours(2))
                .build();

        when(transportationRepository.findById(2L)).thenReturn(Optional.of(testTransportationInTransit));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(transportationService.update(2L, updateData))
                .expectNextMatches(transportation -> {
                    assertEquals(existingActualDeparture, transportation.getActualDeparture());
                    return true;
                })
                .verifyComplete();
    }
}