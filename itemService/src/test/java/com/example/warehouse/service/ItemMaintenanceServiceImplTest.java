package com.example.warehouse.service;

import com.example.warehouse.application.ports.input.ItemMaintenanceServiceImpl;
import com.example.warehouse.application.ports.input.ItemServiceImpl;
import com.example.warehouse.infrastructure.client.UserServiceClient;
import com.example.warehouse.infrastructure.persistence.entity.Item;
import com.example.warehouse.infrastructure.persistence.entity.ItemMaintenance;
import com.example.warehouse.infrastructure.persistence.entity.User;
import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.domain.enumeration.ItemType;
import com.example.warehouse.domain.enumeration.MaintenanceStatus;
import com.example.warehouse.domain.enumeration.RoleType;
import com.example.warehouse.exception.ItemMaintenanceNotFoundException;
import com.example.warehouse.application.ports.output.ItemMaintenanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemMaintenanceServiceImplTest {

    @Mock
    private ItemMaintenanceRepository itemMaintenanceRepository;

    @Mock
    private ItemServiceImpl itemService;

    @Mock
    private UserServiceClient userService;

    @InjectMocks
    private ItemMaintenanceServiceImpl itemMaintenanceService;

    private User testTechnician;
    private User testTechnician2;
    private Item testItem;
    private Item testItem2;
    private ItemMaintenance testMaintenance;

    @BeforeEach
    void setUp() {
        testTechnician = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Technician")
                .email("tech1@example.com")
                .role(RoleType.MANAGER)
                .createdAt(LocalDateTime.now())
                .build();

        testTechnician2 = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Technician")
                .email("tech2@example.com")
                .role(RoleType.MANAGER)
                .createdAt(LocalDateTime.now())
                .build();

        testItem = Item.builder()
                .id(1L)
                .name("Test Laptop")
                .description("High-performance laptop")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN-LAPTOP-001")
                .createdAt(LocalDateTime.now())
                .build();

        testItem2 = Item.builder()
                .id(2L)
                .name("Test Monitor")
                .description("4K Monitor")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.NEEDS_MAINTENANCE)
                .serialNumber("SN-MONITOR-001")
                .createdAt(LocalDateTime.now())
                .build();

        testMaintenance = ItemMaintenance.builder()
                .id(1L)
                .item(testItem)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now().minusDays(1))
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(new BigDecimal("150.50"))
                .description("Routine maintenance and cleaning")
                .status(MaintenanceStatus.COMPLETED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void create_ShouldCreateItemMaintenance_WhenValidData() {
        // Arrange
        ItemMaintenance newMaintenance = ItemMaintenance.builder()
                .item(testItem2)
                .technicianId(testTechnician2.getId())
                .maintenanceDate(LocalDateTime.now().minusHours(2))
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(3))
                .cost(new BigDecimal("75.25"))
                .description("Display calibration")
                .status(MaintenanceStatus.COMPLETED)
                .build();

        ItemMaintenance savedMaintenance = ItemMaintenance.builder()
                .id(2L)
                .item(testItem2)
                .technicianId(testTechnician2.getId())
                .maintenanceDate(LocalDateTime.now().minusHours(2))
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(3))
                .cost(new BigDecimal("75.25"))
                .description("Display calibration")
                .status(MaintenanceStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        when(itemService.getById(2L)).thenReturn(Mono.just(testItem2));
        when(userService.getUserById(2L)).thenReturn(Mono.just(testTechnician2));
        when(itemMaintenanceRepository.save(newMaintenance)).thenReturn(savedMaintenance);

        // Act & Assert
        StepVerifier.create(itemMaintenanceService.create(newMaintenance))
                .expectNextMatches(maintenance -> {
                    assertNotNull(maintenance.getId());
                    assertEquals(testItem2.getId(), maintenance.getItem().getId());
                    assertEquals(testTechnician2.getId(), maintenance.getTechnicianId());
                    assertEquals(new BigDecimal("75.25"), maintenance.getCost());
                    assertEquals("Display calibration", maintenance.getDescription());
                    assertEquals(MaintenanceStatus.COMPLETED, maintenance.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(itemService).getById(2L);
        verify(userService).getUserById(2L);
        verify(itemMaintenanceRepository).save(newMaintenance);
    }

    @Test
    void getById_ShouldReturnItemMaintenance_WhenExists() {
        // Arrange
        when(itemMaintenanceRepository.findById(1L)).thenReturn(Optional.of(testMaintenance));

        // Act & Assert
        StepVerifier.create(itemMaintenanceService.getById(1L))
                .expectNextMatches(maintenance -> {
                    assertEquals(testMaintenance.getId(), maintenance.getId());
                    assertEquals(testItem.getId(), maintenance.getItem().getId());
                    assertEquals(testTechnician.getId(), maintenance.getTechnicianId());
                    assertEquals(new BigDecimal("150.50"), maintenance.getCost());
                    assertEquals(MaintenanceStatus.COMPLETED, maintenance.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(itemMaintenanceRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowItemMaintenanceNotFoundException_WhenNotFound() {
        // Arrange
        when(itemMaintenanceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(itemMaintenanceService.getById(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemMaintenanceNotFoundException &&
                                throwable.getMessage().contains("Item maintenance not found with ID: 999")
                )
                .verify();

        verify(itemMaintenanceRepository).findById(999L);
    }

    @Test
    void update_ShouldUpdateItem_WhenItemChanged() {
        ItemMaintenance updateData = ItemMaintenance.builder()
                .item(testItem2)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now().minusDays(1))
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(new BigDecimal("150.50"))
                .description("Routine maintenance")
                .status(MaintenanceStatus.COMPLETED)
                .build();

        ItemMaintenance updatedMaintenance = ItemMaintenance.builder()
                .id(1L)
                .item(testItem2)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now().minusDays(1))
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(new BigDecimal("150.50"))
                .description("Routine maintenance")
                .status(MaintenanceStatus.COMPLETED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(itemMaintenanceRepository.findById(1L)).thenReturn(Optional.of(testMaintenance));
        when(itemService.getById(2L)).thenReturn(Mono.just(testItem2));
        when(itemMaintenanceRepository.save(any(ItemMaintenance.class))).thenReturn(updatedMaintenance);

        StepVerifier.create(itemMaintenanceService.update(1L, updateData))
                .verifyComplete();

        verify(itemMaintenanceRepository).findById(1L);
        verify(itemService).getById(2L);
        verify(itemMaintenanceRepository).save(any(ItemMaintenance.class));
    }

    @Test
    void update_ShouldThrowItemMaintenanceNotFoundException_WhenNotFound() {
        ItemMaintenance updateData = ItemMaintenance.builder()
                .item(testItem)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now())
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(new BigDecimal("100.00"))
                .description("Test update")
                .status(MaintenanceStatus.PLANNED)
                .build();

        when(itemMaintenanceRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(itemMaintenanceService.update(999L, updateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemMaintenanceNotFoundException &&
                                throwable.getMessage().contains("Item maintenance not found with ID: 999")
                )
                .verify();

        verify(itemMaintenanceRepository).findById(999L);
        verify(itemService, never()).getById(anyLong());
        verify(userService, never()).getUserById(anyLong());
        verify(itemMaintenanceRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteItemMaintenance_WhenExists() {
        when(itemMaintenanceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(itemMaintenanceRepository).deleteById(1L);

        StepVerifier.create(itemMaintenanceService.delete(1L))
                .verifyComplete();

        verify(itemMaintenanceRepository).existsById(1L);
        verify(itemMaintenanceRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowItemMaintenanceNotFoundException_WhenNotFound() {
        // Arrange
        when(itemMaintenanceRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        StepVerifier.create(itemMaintenanceService.delete(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemMaintenanceNotFoundException &&
                                throwable.getMessage().contains("Item maintenance not found with ID: 999")
                )
                .verify();

        verify(itemMaintenanceRepository).existsById(999L);
        verify(itemMaintenanceRepository, never()).deleteById(anyLong());
    }

    @Test
    void findMaintenancesByFilters_ShouldReturnFilteredResults_WithItemIdFilter() {
        List<ItemMaintenance> maintenances = List.of(testMaintenance);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "maintenanceDate"));
        Page<ItemMaintenance> page = new PageImpl<>(maintenances, pageable, maintenances.size());

        when(itemMaintenanceRepository.findByItemId(1L, pageable)).thenReturn(page);

        StepVerifier.create(itemMaintenanceService.findMaintenancesByFilters(1L, null, pageable))
                .expectNextMatches(maintenance -> {
                    assertEquals(testItem.getId(), maintenance.getItem().getId());
                    return true;
                })
                .verifyComplete();

        verify(itemMaintenanceRepository).findByItemId(1L, pageable);
    }

    @Test
    void findMaintenancesByFilters_ShouldReturnFilteredResults_WithStatusFilter() {
        List<ItemMaintenance> maintenances = List.of(testMaintenance);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "maintenanceDate"));
        Page<ItemMaintenance> page = new PageImpl<>(maintenances, pageable, maintenances.size());

        when(itemMaintenanceRepository.findByStatus(MaintenanceStatus.COMPLETED, pageable)).thenReturn(page);

        StepVerifier.create(itemMaintenanceService.findMaintenancesByFilters(null, MaintenanceStatus.COMPLETED, pageable))
                .expectNextMatches(maintenance -> {
                    assertEquals(MaintenanceStatus.COMPLETED, maintenance.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(itemMaintenanceRepository).findByStatus(MaintenanceStatus.COMPLETED, pageable);
    }

    @Test
    void findMaintenancesByFilters_ShouldReturnFilteredResults_WithBothFilters() {
        List<ItemMaintenance> maintenances = List.of(testMaintenance);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "maintenanceDate"));
        Page<ItemMaintenance> page = new PageImpl<>(maintenances, pageable, maintenances.size());

        when(itemMaintenanceRepository.findByItemIdAndStatus(1L, MaintenanceStatus.COMPLETED, pageable))
                .thenReturn(page);

        StepVerifier.create(itemMaintenanceService.findMaintenancesByFilters(1L, MaintenanceStatus.COMPLETED, pageable))
                .expectNextMatches(maintenance -> {
                    assertEquals(testItem.getId(), maintenance.getItem().getId());
                    assertEquals(MaintenanceStatus.COMPLETED, maintenance.getStatus());
                    return true;
                })
                .verifyComplete();

        verify(itemMaintenanceRepository).findByItemIdAndStatus(1L, MaintenanceStatus.COMPLETED, pageable);
    }

    @Test
    void findMaintenancesByFilters_ShouldReturnAll_WhenNoFilters() {
        List<ItemMaintenance> maintenances = List.of(testMaintenance);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "maintenanceDate"));
        Page<ItemMaintenance> page = new PageImpl<>(maintenances, pageable, maintenances.size());

        when(itemMaintenanceRepository.findAll(pageable)).thenReturn(page);

        StepVerifier.create(itemMaintenanceService.findMaintenancesByFilters(null, null, pageable))
                .expectNextMatches(maintenance -> {
                    assertEquals(testMaintenance.getId(), maintenance.getId());
                    return true;
                })
                .verifyComplete();

        verify(itemMaintenanceRepository).findAll(pageable);
    }

    @Test
    void countMaintenancesByFilters_ShouldReturnCount_WithItemIdFilter() {
        when(itemMaintenanceRepository.countByItemId(1L)).thenReturn(3L);

        StepVerifier.create(itemMaintenanceService.countMaintenancesByFilters(1L, null))
                .expectNextMatches(count -> count == 3L)
                .verifyComplete();

        verify(itemMaintenanceRepository).countByItemId(1L);
    }

    @Test
    void countMaintenancesByFilters_ShouldReturnCount_WithStatusFilter() {
        when(itemMaintenanceRepository.countByStatus(MaintenanceStatus.COMPLETED)).thenReturn(5L);

        StepVerifier.create(itemMaintenanceService.countMaintenancesByFilters(null, MaintenanceStatus.COMPLETED))
                .expectNextMatches(count -> count == 5L)
                .verifyComplete();

        verify(itemMaintenanceRepository).countByStatus(MaintenanceStatus.COMPLETED);
    }

    @Test
    void countMaintenancesByFilters_ShouldReturnCount_WithBothFilters() {
        when(itemMaintenanceRepository.countByItemIdAndStatus(1L, MaintenanceStatus.COMPLETED)).thenReturn(2L);

        StepVerifier.create(itemMaintenanceService.countMaintenancesByFilters(1L, MaintenanceStatus.COMPLETED))
                .expectNextMatches(count -> count == 2L)
                .verifyComplete();

        verify(itemMaintenanceRepository).countByItemIdAndStatus(1L, MaintenanceStatus.COMPLETED);
    }

    @Test
    void countMaintenancesByFilters_ShouldReturnTotalCount_WhenNoFilters() {
        when(itemMaintenanceRepository.count()).thenReturn(10L);

        StepVerifier.create(itemMaintenanceService.countMaintenancesByFilters(null, null))
                .expectNextMatches(count -> count == 10L)
                .verifyComplete();

        verify(itemMaintenanceRepository).count();
    }

    @Test
    void findByTechnician_ShouldReturnTechnicianMaintenance() {
        List<ItemMaintenance> maintenances = List.of(testMaintenance);
        Pageable pageable = PageRequest.of(0, 10);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "maintenanceDate"));
        Page<ItemMaintenance> page = new PageImpl<>(maintenances, pageRequest, maintenances.size());

        when(itemMaintenanceRepository.findByTechnicianId(1L, pageRequest)).thenReturn(page);

        StepVerifier.create(itemMaintenanceService.findByTechnician(1L, pageable))
                .expectNextMatches(maintenance -> {
                    assertEquals(testTechnician.getId(), maintenance.getTechnicianId());
                    return true;
                })
                .verifyComplete();

        verify(itemMaintenanceRepository).findByTechnicianId(1L, pageRequest);
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        when(itemMaintenanceRepository.countByStatus(MaintenanceStatus.COMPLETED)).thenReturn(5L);

        StepVerifier.create(itemMaintenanceService.countByStatus(MaintenanceStatus.COMPLETED))
                .expectNextMatches(count -> count == 5L)
                .verifyComplete();

        verify(itemMaintenanceRepository).countByStatus(MaintenanceStatus.COMPLETED);
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        ItemMaintenance updatedMaintenance = ItemMaintenance.builder()
                .id(1L)
                .item(testItem)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now().minusDays(1))
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(new BigDecimal("150.50"))
                .description("Routine maintenance and cleaning")
                .status(MaintenanceStatus.CANCELLED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(itemMaintenanceRepository.findById(1L)).thenReturn(Optional.of(testMaintenance));
        when(itemMaintenanceRepository.save(any(ItemMaintenance.class))).thenReturn(updatedMaintenance);

        StepVerifier.create(itemMaintenanceService.updateStatus(1L, MaintenanceStatus.CANCELLED))
                .verifyComplete();

        verify(itemMaintenanceRepository).findById(1L);
        verify(itemMaintenanceRepository).save(argThat(maintenance ->
                maintenance.getStatus() == MaintenanceStatus.CANCELLED
        ));
    }

    @Test
    void updateStatus_ShouldThrowItemMaintenanceNotFoundException_WhenNotFound() {
        when(itemMaintenanceRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(itemMaintenanceService.updateStatus(999L, MaintenanceStatus.COMPLETED))
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemMaintenanceNotFoundException &&
                                throwable.getMessage().contains("Item maintenance not found with ID: 999")
                )
                .verify();

        verify(itemMaintenanceRepository).findById(999L);
        verify(itemMaintenanceRepository, never()).save(any());
    }

    @Test
    void create_ShouldHandleZeroCost() {
        ItemMaintenance maintenanceWithZeroCost = ItemMaintenance.builder()
                .item(testItem2)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now())
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(BigDecimal.ZERO)
                .description("Warranty maintenance")
                .status(MaintenanceStatus.COMPLETED)
                .build();

        ItemMaintenance savedMaintenance = ItemMaintenance.builder()
                .id(3L)
                .item(testItem2)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now())
                .nextMaintenanceDate(LocalDateTime.now().plusMonths(6))
                .cost(BigDecimal.ZERO)
                .description("Warranty maintenance")
                .status(MaintenanceStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        when(itemService.getById(2L)).thenReturn(Mono.just(testItem2));
        when(userService.getUserById(1L)).thenReturn(Mono.just(testTechnician));
        when(itemMaintenanceRepository.save(maintenanceWithZeroCost)).thenReturn(savedMaintenance);

        StepVerifier.create(itemMaintenanceService.create(maintenanceWithZeroCost))
                .expectNextMatches(maintenance -> {
                    assertEquals(BigDecimal.ZERO, maintenance.getCost());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void create_ShouldHandleNullNextMaintenanceDate() {
        ItemMaintenance maintenanceWithoutNextDate = ItemMaintenance.builder()
                .item(testItem2)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now())
                .nextMaintenanceDate(null)
                .cost(new BigDecimal("50.00"))
                .description("One-time repair")
                .status(MaintenanceStatus.COMPLETED)
                .build();

        ItemMaintenance savedMaintenance = ItemMaintenance.builder()
                .id(4L)
                .item(testItem2)
                .technicianId(testTechnician.getId())
                .maintenanceDate(LocalDateTime.now())
                .nextMaintenanceDate(null)
                .cost(new BigDecimal("50.00"))
                .description("One-time repair")
                .status(MaintenanceStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        when(itemService.getById(2L)).thenReturn(Mono.just(testItem2));
        when(userService.getUserById(1L)).thenReturn(Mono.just(testTechnician));
        when(itemMaintenanceRepository.save(maintenanceWithoutNextDate)).thenReturn(savedMaintenance);

        StepVerifier.create(itemMaintenanceService.create(maintenanceWithoutNextDate))
                .expectNextMatches(maintenance -> {
                    assertNull(maintenance.getNextMaintenanceDate());
                    return true;
                })
                .verifyComplete();
    }
}