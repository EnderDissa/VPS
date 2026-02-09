package com.example.warehouse.service;

import com.example.warehouse.application.input.TransportationServiceImpl;
import com.example.warehouse.domain.enumeration.*;
import com.example.warehouse.domain.exception.OperationNotAllowedException;
import com.example.warehouse.domain.exception.TransportationNotFoundException;
import com.example.warehouse.infrastructure.client.ItemServiceClient;
import com.example.warehouse.infrastructure.client.UserServiceClient;
import com.example.warehouse.infrastructure.persistence.entity.*;
import com.example.warehouse.application.output.TransportationRepository;
import com.example.warehouse.application.input.interfaces.StorageService;
import com.example.warehouse.application.input.interfaces.VehicleService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

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
    private MockedStatic<ReactiveSecurityContextHolder> utilities;
    private SecurityContext ctx;

    @BeforeEach
    void setUp() {

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

        testTransportationPlanned = Transportation.builder()
                .id(1L)
                .itemId(testItem1.getId())
                .vehicle(testVehicle1)
                .driverId(testDriver1.getId())
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
                .itemId(testItem2.getId())
                .vehicle(testVehicle2)
                .driverId(testDriver2.getId())
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
                .itemId(testItem1.getId())
                .vehicle(testVehicle1)
                .driverId(testDriver1.getId())
                .fromStorage(testStorage3)
                .toStorage(testStorage1)
                .status(TransportStatus.DELIVERED)
                .scheduledDeparture(LocalDateTime.now().minusDays(3))
                .actualDeparture(LocalDateTime.now().minusDays(3))
                .scheduledArrival(LocalDateTime.now().minusDays(2))
                .actualArrival(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(4))
                .build();

        ctx =  Mockito.mock(org.springframework.security.core.context.SecurityContextImpl.class);

        utilities = Mockito.mockStatic(ReactiveSecurityContextHolder.class);
        utilities.when(ReactiveSecurityContextHolder::getContext).thenReturn(Mono.just(ctx));

        ReactiveSecurityContextHolder
                .getContext()
                .map(c -> {System.out.println(c);return c;});
    }

    @AfterEach
    void tearDown() {
        utilities.close();
    }

    @Test
    void create_ShouldThrowOperationNotAllowedException_WhenSameFromAndToStorage() {

        Transportation newTransportation = Transportation.builder()
                .itemId(testItem1.getId())
                .vehicle(testVehicle1)
                .driverId(testDriver1.getId())
                .fromStorage(testStorage1)
                .toStorage(testStorage1)
                .status(TransportStatus.PLANNED)
                .build();


        StepVerifier.create(transportationService.create(newTransportation))
                .expectErrorMatches(throwable ->
                        throwable instanceof OperationNotAllowedException &&
                                throwable.getMessage().contains("From and to storage cannot be the same")
                )
                .verify();

        verify(itemServiceClient, never()).getItemById(anyLong());
        verify(vehicleService, never()).getById(anyLong());
        verify(storageService, never()).getById(anyLong());
        verify(transportationRepository, never()).save(any());
    }



    @Test
    void getById_ShouldReturnTransportation_WhenTransportationExists() {

        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));


        StepVerifier.create(transportationService.getById(1L))
                .expectNextMatches(transportation -> {
                    assertEquals(testTransportationPlanned.getId(), transportation.getId());
                    assertEquals(testItem1.getId(), transportation.getItemId());
                    assertEquals(testVehicle1.getId(), transportation.getVehicle().getId());
                    assertEquals(testDriver1.getId(), transportation.getDriverId());
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

        when(transportationRepository.findById(999L)).thenReturn(Optional.empty());


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

        Transportation updateData = Transportation.builder()
                .itemId(testItem1.getId())
                .vehicle(testVehicle1)
                .driverId(testDriver1.getId())
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .scheduledDeparture(LocalDateTime.now().plusDays(3))
                .scheduledArrival(LocalDateTime.now().plusDays(4))
                .build();

        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));


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
        verify(vehicleService, never()).getById(anyLong());
        verify(storageService, never()).getById(anyLong());
    }


    @Test
    void update_ShouldSetActualDeparture_WhenStatusChangedToInTransit() {

        Transportation updateData = Transportation.builder()
                .itemId(testItem1.getId())
                .vehicle(testVehicle1)
                .driverId(testDriver1.getId())
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.IN_TRANSIT)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(2))
                .build();

        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));


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

        Transportation updateData = Transportation.builder()
                .itemId(testItem2.getId())
                .vehicle(testVehicle2)
                .driverId(testDriver2.getId())
                .fromStorage(testStorage2)
                .toStorage(testStorage3)
                .status(TransportStatus.DELIVERED)
                .scheduledDeparture(LocalDateTime.now().minusHours(2))
                .scheduledArrival(LocalDateTime.now().plusHours(2))
                .build();

        when(transportationRepository.findById(2L)).thenReturn(Optional.of(testTransportationInTransit));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));


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

        Transportation updateData = Transportation.builder()
                .itemId(testItem1.getId())
                .vehicle(testVehicle1)
                .driverId(testDriver1.getId())
                .fromStorage(testStorage1)
                .toStorage(testStorage2)
                .status(TransportStatus.PLANNED)
                .build();

        when(transportationRepository.findById(999L)).thenReturn(Optional.empty());

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

        Transportation updateData = Transportation.builder()
                .itemId(testItem1.getId())
                .vehicle(testVehicle1)
                .driverId(testDriver1.getId())
                .fromStorage(testStorage3)
                .toStorage(testStorage1)
                .status(TransportStatus.DELIVERED)
                .build();

        when(transportationRepository.findById(3L)).thenReturn(Optional.of(testTransportationDelivered));


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
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportationPlanned));
        doNothing().when(transportationRepository).deleteById(1L);


        StepVerifier.create(transportationService.delete(1L))
                .verifyComplete();

        verify(transportationRepository).findById(1L);
        verify(transportationRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowTransportationNotFoundException_WhenTransportationNotFound() {

        when(transportationRepository.findById(999L)).thenReturn(Optional.empty());


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

        when(transportationRepository.findById(3L)).thenReturn(Optional.of(testTransportationDelivered));


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

        List<Transportation> transportations = List.of(testTransportationPlanned, testTransportationInTransit, testTransportationDelivered);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findAll(pageable)).thenReturn(page);


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

        List<Transportation> transportations = List.of(testTransportationPlanned);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findByStatus(TransportStatus.PLANNED, pageable)).thenReturn(page);


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

        List<Transportation> transportations = List.of(testTransportationPlanned, testTransportationDelivered);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findByItemId(1L, pageable)).thenReturn(page);


        StepVerifier.create(transportationService.findPage(0, 10, null, 1L, null, null))
                .expectNextMatches(result -> {
                    assertEquals(2, result.getTotalElements());
                    assertTrue(result.getContent().stream().allMatch(t -> t.getItemId().equals(1L)));
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findByItemId(1L, pageable);
    }

    @Test
    void findPage_ShouldReturnFilteredByMultipleCriteria_WhenMultipleFiltersApplied() {

        List<Transportation> transportations = List.of(testTransportationPlanned);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(transportations, pageable, transportations.size());

        when(transportationRepository.findByStatusAndItemIdAndFromStorageIdAndToStorageId(
                TransportStatus.PLANNED, 1L, 1L, 2L, pageable))
                .thenReturn(page);


        StepVerifier.create(transportationService.findPage(0, 10, TransportStatus.PLANNED, 1L, 1L, 2L))
                .expectNextMatches(result -> {
                    assertEquals(1, result.getTotalElements());
                    Transportation transportation = result.getContent().get(0);
                    assertEquals(TransportStatus.PLANNED, transportation.getStatus());
                    assertEquals(1L, transportation.getItemId());
                    assertEquals(1L, transportation.getFromStorage().getId());
                    assertEquals(2L, transportation.getToStorage().getId());
                    return true;
                })
                .verifyComplete();

        verify(transportationRepository).findByStatusAndItemIdAndFromStorageIdAndToStorageId(
                TransportStatus.PLANNED, 1L, 1L, 2L, pageable);
    }

    @Test
    void findPage_ShouldReturnPagedResults_WhenPageSizeSmallerThanTotal() {

        List<Transportation> firstPage = List.of(testTransportationPlanned, testTransportationInTransit);
        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(firstPage, pageable, 3);

        when(transportationRepository.findAll(pageable)).thenReturn(page);


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

        List<Transportation> emptyList = List.of();
        PageRequest pageable = PageRequest.of(10, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transportation> page = new PageImpl<>(emptyList, pageable, 3);

        when(transportationRepository.findAll(pageable)).thenReturn(page);

        StepVerifier.create(transportationService.findPage(10, 10, null, null, null, null))
                .expectNextMatches(result -> {
                    assertEquals(3, result.getTotalElements());
                    assertTrue(result.getContent().isEmpty());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void update_ShouldNotSetActualTimes_WhenTheyAreAlreadySet() {
        LocalDateTime existingActualDeparture = testTransportationInTransit.getActualDeparture();

        Transportation updateData = Transportation.builder()
                .itemId(testItem2.getId())
                .vehicle(testVehicle2)
                .driverId(testDriver2.getId())
                .fromStorage(testStorage2)
                .toStorage(testStorage3)
                .status(TransportStatus.IN_TRANSIT)
                .scheduledDeparture(LocalDateTime.now().minusHours(2))
                .scheduledArrival(LocalDateTime.now().plusHours(2))
                .build();

        when(transportationRepository.findById(2L)).thenReturn(Optional.of(testTransportationInTransit));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(transportationService.update(2L, updateData))
                .expectNextMatches(transportation -> {
                    assertEquals(existingActualDeparture, transportation.getActualDeparture());
                    return true;
                })
                .verifyComplete();
    }
}