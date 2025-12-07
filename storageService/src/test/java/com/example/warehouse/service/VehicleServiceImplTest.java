package com.example.warehouse.service;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.exception.DuplicateLicensePlateException;
import com.example.warehouse.exception.VehicleNotFoundException;
import com.example.warehouse.repository.VehicleRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle testVehicle;
    private Vehicle testVehicle2;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .id(1L)
                .brand("Volvo")
                .model("FH16")
                .licensePlate("ABC123")
                .year(2022)
                .capacity(25000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        testVehicle2 = Vehicle.builder()
                .id(2L)
                .brand("MAN")
                .model("TGX")
                .licensePlate("XYZ789")
                .year(2021)
                .capacity(18000)
                .status(VehicleStatus.IN_USE)
                .build();
    }

    @Test
    void create_ShouldCreateVehicle_WhenValidData() {
        // Arrange
        Vehicle newVehicle = Vehicle.builder()
                .brand("Mercedes")
                .model("Actros")
                .licensePlate("DEF456")
                .year(2023)
                .capacity(22000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        Vehicle savedVehicle = Vehicle.builder()
                .id(3L)
                .brand("Mercedes")
                .model("Actros")
                .licensePlate("DEF456")
                .year(2023)
                .capacity(22000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        when(vehicleRepository.existsByLicensePlate("DEF456")).thenReturn(false);
        when(vehicleRepository.save(newVehicle)).thenReturn(savedVehicle);

        // Act & Assert
        StepVerifier.create(vehicleService.create(newVehicle))
                .expectNextMatches(vehicle -> {
                    assertNotNull(vehicle.getId());
                    assertEquals("Mercedes", vehicle.getBrand());
                    assertEquals("Actros", vehicle.getModel());
                    assertEquals("DEF456", vehicle.getLicensePlate());
                    assertEquals(2023, vehicle.getYear());
                    assertEquals(22000, vehicle.getCapacity());
                    assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).existsByLicensePlate("DEF456");
        verify(vehicleRepository).save(newVehicle);
    }

    @Test
    void create_ShouldThrowDuplicateLicensePlateException_WhenDuplicateLicensePlate() {
        // Arrange
        Vehicle duplicateVehicle = Vehicle.builder()
                .brand("Scania")
                .model("R500")
                .licensePlate("ABC123")
                .year(2022)
                .capacity(24000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        when(vehicleRepository.existsByLicensePlate("ABC123")).thenReturn(true);

        // Act & Assert
        StepVerifier.create(vehicleService.create(duplicateVehicle))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateLicensePlateException &&
                                throwable.getMessage().contains("Vehicle with license plate 'ABC123' already exists")
                )
                .verify();

        verify(vehicleRepository).existsByLicensePlate("ABC123");
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnVehicle_WhenExists() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        StepVerifier.create(vehicleService.getById(1L))
                .expectNextMatches(vehicle -> {
                    assertEquals(testVehicle.getId(), vehicle.getId());
                    assertEquals(testVehicle.getBrand(), vehicle.getBrand());
                    assertEquals(testVehicle.getModel(), vehicle.getModel());
                    assertEquals(testVehicle.getLicensePlate(), vehicle.getLicensePlate());
                    assertEquals(testVehicle.getStatus(), vehicle.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowVehicleNotFoundException_WhenNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(vehicleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(vehicleService.getById(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof VehicleNotFoundException &&
                                throwable.getMessage().contains("Vehicle not found with ID: " + nonExistentId)
                )
                .verify();

        verify(vehicleRepository).findById(nonExistentId);
    }

    @Test
    void update_ShouldUpdateVehicle_WhenValidData() {
        // Arrange
        Vehicle updateData = Vehicle.builder()
                .brand("Volvo")
                .model("FH16 Electric")
                .licensePlate("ABC123")
                .year(2023)
                .capacity(26000)
                .status(VehicleStatus.IN_USE)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(vehicleService.update(1L, updateData))
                .expectNextMatches(vehicle -> {
                    assertEquals("FH16 Electric", vehicle.getModel());
                    assertEquals(2023, vehicle.getYear());
                    assertEquals(26000, vehicle.getCapacity());
                    assertEquals(VehicleStatus.IN_USE, vehicle.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).existsByLicensePlateAndIdNot(anyString(), anyLong());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void update_ShouldUpdateLicensePlate_WhenNewLicensePlateIsUnique() {
        // Arrange
        Vehicle updateData = Vehicle.builder()
                .brand("Volvo")
                .model("FH16")
                .licensePlate("NEW123")
                .year(2022)
                .capacity(25000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.existsByLicensePlateAndIdNot("NEW123", 1L)).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(vehicleService.update(1L, updateData))
                .expectNextMatches(vehicle -> {
                    assertEquals("NEW123", vehicle.getLicensePlate());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).existsByLicensePlateAndIdNot("NEW123", 1L);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void update_ShouldThrowDuplicateLicensePlateException_WhenDuplicateLicensePlate() {
        // Arrange
        Vehicle updateData = Vehicle.builder()
                .brand("Volvo")
                .model("FH16")
                .licensePlate("XYZ789")
                .year(2022)
                .capacity(25000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.existsByLicensePlateAndIdNot("XYZ789", 1L)).thenReturn(true);

        // Act & Assert
        StepVerifier.create(vehicleService.update(1L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateLicensePlateException &&
                                throwable.getMessage().contains("Vehicle with license plate 'XYZ789' already exists")
                )
                .verify();

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).existsByLicensePlateAndIdNot("XYZ789", 1L);
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowVehicleNotFoundException_WhenVehicleNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        Vehicle updateData = Vehicle.builder()
                .brand("Brand")
                .model("Model")
                .licensePlate("PLATE999")
                .year(2022)
                .capacity(10000)
                .status(VehicleStatus.AVAILABLE)
                .build();

        when(vehicleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(vehicleService.update(nonExistentId, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof VehicleNotFoundException &&
                                throwable.getMessage().contains("Vehicle not found with ID: " + nonExistentId)
                )
                .verify();

        verify(vehicleRepository).findById(nonExistentId);
        verify(vehicleRepository, never()).existsByLicensePlateAndIdNot(anyString(), anyLong());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteVehicle_WhenExists() {
        // Arrange
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        // Act & Assert
        StepVerifier.create(vehicleService.delete(1L))
                .verifyComplete();

        verify(vehicleRepository).existsById(1L);
        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowVehicleNotFoundException_WhenNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(vehicleRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        StepVerifier.create(vehicleService.delete(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof VehicleNotFoundException &&
                                throwable.getMessage().contains("Vehicle not found with ID: " + nonExistentId)
                )
                .verify();

        verify(vehicleRepository).existsById(nonExistentId);
        verify(vehicleRepository, never()).deleteById(anyLong());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithStatusFilter() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("brand").and(Sort.by("model")));
        Page<Vehicle> page = new PageImpl<>(vehicles, pageable, vehicles.size());

        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE, pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findPage(0, 10, VehicleStatus.AVAILABLE, null, null))
                .expectNextMatches(result -> {
                    assertEquals(1, result.getTotalElements());
                    assertEquals(VehicleStatus.AVAILABLE, result.getContent().get(0).getStatus());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findByStatus(VehicleStatus.AVAILABLE, pageable);
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithBrandFilter() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("brand").and(Sort.by("model")));
        Page<Vehicle> page = new PageImpl<>(vehicles, pageable, vehicles.size());

        when(vehicleRepository.findByBrandContainingIgnoreCase("Volvo", pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findPage(0, 10, null, "Volvo", null))
                .expectNextMatches(result -> {
                    assertEquals(1, result.getTotalElements());
                    assertEquals("Volvo", result.getContent().get(0).getBrand());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findByBrandContainingIgnoreCase("Volvo", pageable);
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithMultipleFilters() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("brand").and(Sort.by("model")));
        Page<Vehicle> page = new PageImpl<>(vehicles, pageable, vehicles.size());

        when(vehicleRepository.findByStatusAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                VehicleStatus.AVAILABLE, "Volvo", "FH16", pageable))
                .thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findPage(0, 10, VehicleStatus.AVAILABLE, "Volvo", "FH16"))
                .expectNextMatches(result -> {
                    assertEquals(1, result.getTotalElements());
                    Vehicle vehicle = result.getContent().get(0);
                    assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
                    assertEquals("Volvo", vehicle.getBrand());
                    assertEquals("FH16", vehicle.getModel());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findByStatusAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                VehicleStatus.AVAILABLE, "Volvo", "FH16", pageable);
    }

    @Test
    void findPage_ShouldReturnAllVehicles_WhenNoFilters() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle, testVehicle2);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("brand").and(Sort.by("model")));
        Page<Vehicle> page = new PageImpl<>(vehicles, pageable, vehicles.size());

        when(vehicleRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findPage(0, 10, null, null, null))
                .expectNextMatches(result -> {
                    assertEquals(2, result.getTotalElements());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findAll(pageable);
    }

    @Test
    void findByLicensePlate_ShouldReturnVehicle_WhenExists() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("ABC123")).thenReturn(Optional.of(testVehicle));

        // Act & Assert
        StepVerifier.create(vehicleService.findByLicensePlate("ABC123"))
                .expectNextMatches(vehicle -> {
                    assertEquals("ABC123", vehicle.getLicensePlate());
                    assertEquals("Volvo", vehicle.getBrand());
                    return true;
                })
                .verifyComplete();

        verify(vehicleRepository).findByLicensePlate("ABC123");
    }

    @Test
    void findByLicensePlate_ShouldThrowVehicleNotFoundException_WhenNotFound() {
        // Arrange
        when(vehicleRepository.findByLicensePlate("NONEXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(vehicleService.findByLicensePlate("NONEXISTENT"))
                .expectErrorMatches(throwable ->
                        throwable instanceof VehicleNotFoundException &&
                                throwable.getMessage().contains("Vehicle not found with license plate: NONEXISTENT")
                )
                .verify();

        verify(vehicleRepository).findByLicensePlate("NONEXISTENT");
    }

    @Test
    void create_ShouldHandleZeroValues_WhenYearAndCapacityAreZero() {
        // Arrange
        Vehicle vehicleWithZeroValues = Vehicle.builder()
                .brand("Tesla")
                .model("Semi")
                .licensePlate("ZERO001")
                .year(0)
                .capacity(0)
                .status(VehicleStatus.AVAILABLE)
                .build();

        Vehicle savedVehicle = Vehicle.builder()
                .id(4L)
                .brand("Tesla")
                .model("Semi")
                .licensePlate("ZERO001")
                .year(0)
                .capacity(0)
                .status(VehicleStatus.AVAILABLE)
                .build();

        when(vehicleRepository.existsByLicensePlate("ZERO001")).thenReturn(false);
        when(vehicleRepository.save(vehicleWithZeroValues)).thenReturn(savedVehicle);

        // Act & Assert
        StepVerifier.create(vehicleService.create(vehicleWithZeroValues))
                .expectNextMatches(vehicle -> {
                    assertEquals(0, vehicle.getYear());
                    assertEquals(0, vehicle.getCapacity());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void update_ShouldHandleNullYearAndCapacity() {
        // Arrange
        Vehicle updateData = Vehicle.builder()
                .brand("Volvo")
                .model("FH16")
                .licensePlate("ABC123")
                .year(null)
                .capacity(null)
                .status(VehicleStatus.AVAILABLE)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(vehicleService.update(1L, updateData))
                .expectNextMatches(vehicle -> {
                    assertNull(vehicle.getYear());
                    assertNull(vehicle.getCapacity());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void findPage_ShouldReturnEmptyPage_WhenNoMatches() {
        // Arrange
        List<Vehicle> emptyList = List.of();
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("brand").and(Sort.by("model")));
        Page<Vehicle> page = new PageImpl<>(emptyList, pageable, 0);

        when(vehicleRepository.findByStatus(VehicleStatus.OUT_OF_SERVICE, pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findPage(0, 10, VehicleStatus.OUT_OF_SERVICE, null, null))
                .expectNextMatches(result -> {
                    assertEquals(0, result.getTotalElements());
                    assertTrue(result.getContent().isEmpty());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void findPage_ShouldReturnPagedResults() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle, testVehicle2);
        PageRequest pageable = PageRequest.of(0, 2, Sort.by("brand").and(Sort.by("model")));
        Page<Vehicle> page = new PageImpl<>(vehicles, pageable, 5);

        when(vehicleRepository.findAll(pageable)).thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findPage(0, 2, null, null, null))
                .expectNextMatches(result -> {
                    assertEquals(5, result.getTotalElements());
                    assertEquals(2, result.getContent().size());
                    assertEquals(3, result.getTotalPages());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void findByStatus_ShouldReturnFluxOfVehicles() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle);
        Page<Vehicle> page = new PageImpl<>(vehicles);

        when(vehicleRepository.findByStatus(eq(VehicleStatus.AVAILABLE), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findByStatus(VehicleStatus.AVAILABLE))
                .expectNextMatches(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE)
                .verifyComplete();
    }

    @Test
    void findAvailableVehicles_ShouldReturnFluxOfAvailableVehicles() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle);
        Page<Vehicle> page = new PageImpl<>(vehicles);

        when(vehicleRepository.findByStatus(eq(VehicleStatus.AVAILABLE), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        StepVerifier.create(vehicleService.findAvailableVehicles())
                .expectNextMatches(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE)
                .verifyComplete();
    }
}