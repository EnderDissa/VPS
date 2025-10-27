package com.example.warehouse.service;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.repository.VehicleRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest

@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class VehicleServiceImplIntegrationTest {

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
    private VehicleServiceImpl vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle testVehicle;
    private Vehicle testVehicle2;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .brand("Volvo")
                .model("FH16")
                .licensePlate("ABC123")
                .year(2022)
                .capacity(25000)
                .status(VehicleStatus.AVAILABLE)
                .build();
        testVehicle = vehicleRepository.save(testVehicle);

        testVehicle2 = Vehicle.builder()
                .brand("MAN")
                .model("TGX")
                .licensePlate("XYZ789")
                .year(2021)
                .capacity(18000)
                .status(VehicleStatus.IN_USE)
                .build();
        testVehicle2 = vehicleRepository.save(testVehicle2);
    }

    @Test
    void create_ShouldCreateVehicle_WhenValidData() {
        VehicleDTO newVehicleDTO = new VehicleDTO(
                null,
                "Mercedes",
                "Actros",
                "DEF456",
                2023,
                22000,
                VehicleStatus.AVAILABLE
        );

        VehicleDTO result = vehicleService.create(newVehicleDTO);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("Mercedes", result.brand());
        assertEquals("Actros", result.model());
        assertEquals("DEF456", result.licensePlate());
        assertEquals(2023, result.year());
        assertEquals(22000, result.capacity());
        assertEquals(VehicleStatus.AVAILABLE, result.status());

        Vehicle savedVehicle = vehicleRepository.findById(result.id()).orElseThrow();
        assertEquals("Mercedes", savedVehicle.getBrand());
        assertEquals("DEF456", savedVehicle.getLicensePlate());
    }

    @Test
    void create_ShouldThrowException_WhenDuplicateLicensePlate() {
        VehicleDTO duplicateVehicleDTO = new VehicleDTO(
                null,
                "Scania",
                "R500",
                "ABC123",
                2022,
                24000,
                VehicleStatus.AVAILABLE
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.create(duplicateVehicleDTO));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void create_ShouldSetDefaultStatus_WhenStatusNotProvided() {
        VehicleDTO vehicleWithoutStatus = new VehicleDTO(
                null,
                "Iveco",
                "Stralis",
                "GHI789",
                2022,
                20000,
                null
        );

        VehicleDTO result = vehicleService.create(vehicleWithoutStatus);

        assertEquals(VehicleStatus.AVAILABLE, result.status());
    }

    @Test
    void getById_ShouldReturnVehicle_WhenExists() {
        VehicleDTO result = vehicleService.getById(testVehicle.getId());

        assertNotNull(result);
        assertEquals(testVehicle.getId(), result.id());
        assertEquals(testVehicle.getBrand(), result.brand());
        assertEquals(testVehicle.getModel(), result.model());
        assertEquals(testVehicle.getLicensePlate(), result.licensePlate());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.getById(nonExistentId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void update_ShouldUpdateVehicle_WhenValidData() {
        VehicleDTO updateDTO = new VehicleDTO(
                testVehicle.getId(),
                "Volvo",
                "FH16 Electric",
                "ABC123",
                2023,
                26000,
                VehicleStatus.IN_USE
        );

        vehicleService.update(testVehicle.getId(), updateDTO);

        Vehicle updatedVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertEquals("FH16 Electric", updatedVehicle.getModel());
        assertEquals(2023, updatedVehicle.getYear());
        assertEquals(26000, updatedVehicle.getCapacity());
        assertEquals(VehicleStatus.IN_USE, updatedVehicle.getStatus());
    }

    @Test
    void update_ShouldUpdateLicensePlate_WhenNewLicensePlateIsUnique() {
        VehicleDTO updateDTO = new VehicleDTO(
                testVehicle.getId(),
                "Volvo",
                "FH16",
                "NEW123",
                2022,
                25000,
                VehicleStatus.AVAILABLE
        );

        vehicleService.update(testVehicle.getId(), updateDTO);

        Vehicle updatedVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertEquals("NEW123", updatedVehicle.getLicensePlate());
    }

    @Test
    void update_ShouldThrowException_WhenDuplicateLicensePlate() {
        VehicleDTO updateDTO = new VehicleDTO(
                testVehicle.getId(),
                "Volvo",
                "FH16",
                "XYZ789",
                2022,
                25000,
                VehicleStatus.AVAILABLE
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.update(testVehicle.getId(), updateDTO));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void update_ShouldNotThrowException_WhenLicensePlateNotChanged() {
        VehicleDTO updateDTO = new VehicleDTO(
                testVehicle.getId(),
                "Volvo",
                "FH16 Updated",
                "ABC123",
                2022,
                25000,
                VehicleStatus.AVAILABLE
        );

        assertDoesNotThrow(() -> vehicleService.update(testVehicle.getId(), updateDTO));

        Vehicle updatedVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertEquals("FH16 Updated", updatedVehicle.getModel());
    }

    @Test
    void update_ShouldThrowException_WhenVehicleNotFound() {
        Long nonExistentId = 999L;
        VehicleDTO updateDTO = new VehicleDTO(
                nonExistentId,
                "Brand",
                "Model",
                "PLATE999",
                2022,
                10000,
                VehicleStatus.AVAILABLE
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.update(nonExistentId, updateDTO));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void delete_ShouldDeleteVehicle_WhenExists() {
        Long vehicleId = testVehicle.getId();

        vehicleService.delete(vehicleId);

        assertFalse(vehicleRepository.existsById(vehicleId));
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.delete(nonExistentId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithStatusFilter() {
        Page<VehicleDTO> result = vehicleService.findPage(0, 10, VehicleStatus.AVAILABLE, null, null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(VehicleStatus.AVAILABLE, result.getContent().get(0).status());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithBrandFilter() {
        Page<VehicleDTO> result = vehicleService.findPage(0, 10, null, "Volvo", null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals("Volvo", result.getContent().get(0).brand());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithModelFilter() {
        Page<VehicleDTO> result = vehicleService.findPage(0, 10, null, null, "FH16");

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals("FH16", result.getContent().get(0).model());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithMultipleFilters() {
        Page<VehicleDTO> result = vehicleService.findPage(0, 10, VehicleStatus.AVAILABLE, "Volvo", "FH16");

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);

        VehicleDTO vehicle = result.getContent().get(0);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.status());
        assertEquals("Volvo", vehicle.brand());
        assertEquals("FH16", vehicle.model());
    }

    @Test
    void findPage_ShouldReturnEmpty_WhenNoMatches() {
        Page<VehicleDTO> result = vehicleService.findPage(0, 10, VehicleStatus.OUT_OF_SERVICE, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findPage_ShouldReturnPagedResults() {
        for (int i = 0; i < 5; i++) {
            Vehicle vehicle = Vehicle.builder()
                    .brand("Brand" + i)
                    .model("Model" + i)
                    .licensePlate("PLATE" + i)
                    .year(2020 + i)
                    .capacity(10000 + i * 1000)
                    .status(VehicleStatus.AVAILABLE)
                    .build();
            vehicleRepository.save(vehicle);
        }

        Page<VehicleDTO> result = vehicleService.findPage(0, 3, null, null, null);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertTrue(result.getTotalElements() >= 7);
    }

    @Test
    void findByStatus_ShouldReturnVehiclesWithGivenStatus() {
        List<VehicleDTO> result = vehicleService.findByStatus(VehicleStatus.AVAILABLE);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(v -> v.status() == VehicleStatus.AVAILABLE));
    }

    @Test
    void findByStatus_ShouldReturnEmpty_WhenNoVehiclesWithStatus() {
        List<VehicleDTO> result = vehicleService.findByStatus(VehicleStatus.OUT_OF_SERVICE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByLicensePlate_ShouldReturnVehicle_WhenExists() {
        VehicleDTO result = vehicleService.findByLicensePlate("ABC123");

        assertNotNull(result);
        assertEquals("ABC123", result.licensePlate());
        assertEquals("Volvo", result.brand());
    }

    @Test
    void findByLicensePlate_ShouldThrowException_WhenNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.findByLicensePlate("NONEXISTENT"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void findAvailableVehicles_ShouldReturnOnlyAvailableVehicles() {
        List<VehicleDTO> result = vehicleService.findAvailableVehicles();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(v -> v.status() == VehicleStatus.AVAILABLE));

        assertTrue(result.stream().noneMatch(v -> v.status() == VehicleStatus.IN_USE));
    }

    @Test
    void create_ShouldHandleZeroValues_WhenYearAndCapacityAreZero() {
        VehicleDTO vehicleWithZeroValues = new VehicleDTO(
                null,
                "Tesla",
                "Semi",
                "ZERO001",
                0,
                0,
                VehicleStatus.AVAILABLE
        );

        VehicleDTO result = vehicleService.create(vehicleWithZeroValues);

        assertNotNull(result);
        assertEquals(0, result.year());
        assertEquals(0, result.capacity());
    }

    @Test
    void update_ShouldHandleNullYearAndCapacity() {
        VehicleDTO updateDTO = new VehicleDTO(
                testVehicle.getId(),
                "Volvo",
                "FH16",
                "ABC123",
                null,
                null,
                VehicleStatus.AVAILABLE
        );

        vehicleService.update(testVehicle.getId(), updateDTO);

        Vehicle updatedVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertNull(updatedVehicle.getYear());
        assertNull(updatedVehicle.getCapacity());
    }
}