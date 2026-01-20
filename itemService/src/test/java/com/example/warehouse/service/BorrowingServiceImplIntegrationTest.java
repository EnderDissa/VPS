package com.example.warehouse.service;

import com.example.warehouse.client.UserServiceClient;
import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.BorrowStatus;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.mapper.BorrowingMapper;
import com.example.warehouse.repository.BorrowingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceImplTest {

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private ItemServiceImpl itemService;

    @Mock
    private UserServiceClient userService;

    @Mock
    private BorrowingMapper mapper;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private User testUser;
    private User testUser2;
    private Item testItem;
    private Item testItemUnavailable;
    private Borrowing testBorrowing;
    private Borrowing testOverdueBorrowing;
    private BorrowingDTO borrowingDTO;
    private Item testItemNeedsMaintenance;
    private Item testItemDecommissioned;
    private Borrowing testBorrowingCancelled;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .secondName("Johnovic")
                .lastName("Doe")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .id(2L)
                .email("test2@example.com")
                .firstName("Jane")
                .secondName("Johnovic")
                .lastName("Smith")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        testItem = Item.builder()
                .id(1L)
                .name("Test Laptop")
                .description("High-performance laptop")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .createdAt(LocalDateTime.now())
                .build();

        testItemUnavailable = Item.builder()
                .id(2L)
                .name("Broken Monitor")
                .description("Needs repair")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.UNDER_REPAIR)
                .serialNumber("SN789012")
                .createdAt(LocalDateTime.now())
                .build();

        testBorrowing = Borrowing.builder()
                .id(1L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(2))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .actualReturnDate(null)
                .purpose("Test borrowing")
                .build();

        testOverdueBorrowing = Borrowing.builder()
                .id(2L)
                .userId(testUser2.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(10))
                .expectedReturnDate(LocalDateTime.now().minusDays(2))
                .actualReturnDate(null)
                .purpose("Overdue borrowing")
                .build();

        borrowingDTO = new BorrowingDTO(
                null,
                1L,
                1L,
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "Test purpose"
        );

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .secondName("Johnovic")
                .lastName("Doe")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .id(2L)
                .email("test2@example.com")
                .firstName("Jane")
                .secondName("Johnovic")
                .lastName("Smith")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        testItem = Item.builder()
                .id(1L)
                .name("Test Laptop")
                .description("High-performance laptop")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("SN123456")
                .createdAt(LocalDateTime.now())
                .build();

        testItemUnavailable = Item.builder()
                .id(2L)
                .name("Broken Monitor")
                .description("Needs repair")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.UNDER_REPAIR)
                .serialNumber("SN789012")
                .createdAt(LocalDateTime.now())
                .build();

        testItemNeedsMaintenance = Item.builder()
                .id(3L)
                .name("Printer")
                .description("Needs maintenance")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.NEEDS_MAINTENANCE)
                .serialNumber("SN345678")
                .createdAt(LocalDateTime.now())
                .build();

        testItemDecommissioned = Item.builder()
                .id(4L)
                .name("Old Computer")
                .description("Decommissioned")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.DECOMMISSIONED)
                .serialNumber("SN901234")
                .createdAt(LocalDateTime.now())
                .build();

        testBorrowing = Borrowing.builder()
                .id(1L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(2))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .actualReturnDate(null)
                .purpose("Test borrowing")
                .build();

        testBorrowingCancelled = Borrowing.builder()
                .id(4L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.CANCELLED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(1))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .actualReturnDate(null)
                .purpose("Cancelled borrowing")
                .build();

        testOverdueBorrowing = Borrowing.builder()
                .id(5L)
                .userId(testUser2.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(10))
                .expectedReturnDate(LocalDateTime.now().minusDays(2))
                .actualReturnDate(null)
                .purpose("Overdue borrowing")
                .build();

    }

    @Test
    void create_ShouldCreateBorrowing_WhenValidData() {
        Borrowing entity = Borrowing.builder()
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .purpose("Test purpose")
                .build();

        Borrowing savedBorrowing = Borrowing.builder()
                .id(3L)
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .status(BorrowStatus.ACTIVE)
                .purpose("Test purpose")
                .build();

        when(mapper.toEntity(borrowingDTO)).thenReturn(entity);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem));
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));
        when(borrowingRepository.countActiveBorrowingsByUser(1L)).thenReturn(2L);
        when(borrowingRepository.save(entity)).thenReturn(savedBorrowing);

        StepVerifier.create(borrowingService.create(borrowingDTO))
                .expectNextMatches(borrowing -> {
                    assertNotNull(borrowing.getId());
                    assertEquals(BorrowStatus.ACTIVE, borrowing.getStatus());
                    assertEquals(testUser.getId(), borrowing.getUserId());
                    assertEquals(testItem.getId(), borrowing.getItem().getId());
                    assertEquals(1, borrowing.getQuantity());
                    assertEquals("Test purpose", borrowing.getPurpose());
                    return true;
                })
                .verifyComplete();

        verify(mapper).toEntity(borrowingDTO);
        verify(itemService).getById(1L);
        verify(userService).getUserById(1L);
        verify(borrowingRepository).countActiveBorrowingsByUser(1L);
        verify(borrowingRepository).save(entity);
    }


    @Test
    void create_ShouldThrowIllegalStateException_WhenUserExceedsBorrowingLimit() {
        Borrowing entity = Borrowing.builder()
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .purpose("Test purpose")
                .build();

        when(mapper.toEntity(borrowingDTO)).thenReturn(entity);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem));
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));
        when(borrowingRepository.countActiveBorrowingsByUser(1L)).thenReturn(5L);

        StepVerifier.create(borrowingService.create(borrowingDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("User has reached maximum active borrowings limit (5)")
                )
                .verify();

        verify(mapper).toEntity(borrowingDTO);
        verify(itemService).getById(1L);
        verify(userService).getUserById(1L);
        verify(borrowingRepository).countActiveBorrowingsByUser(1L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void create_ShouldHandleDefaultValues_WhenNotProvidedInDTO() {
        BorrowingDTO dtoWithoutDefaults = new BorrowingDTO(
                null,
                1L,
                1L,
                null,
                null,
                LocalDateTime.now().plusDays(7),
                null,
                null,
                "Test purpose"
        );

        Borrowing entity = Borrowing.builder()
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .purpose("Test purpose")
                .build();

        Borrowing savedBorrowing = Borrowing.builder()
                .id(4L)
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .status(BorrowStatus.ACTIVE)
                .purpose("Test purpose")
                .build();

        when(mapper.toEntity(dtoWithoutDefaults)).thenReturn(entity);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem));
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));
        when(borrowingRepository.countActiveBorrowingsByUser(1L)).thenReturn(2L);
        when(borrowingRepository.save(entity)).thenReturn(savedBorrowing);

        StepVerifier.create(borrowingService.create(dtoWithoutDefaults))
                .expectNextMatches(borrowing -> {
                    assertEquals(BorrowStatus.ACTIVE, borrowing.getStatus());
                    assertEquals(1, borrowing.getQuantity());
                    assertNotNull(borrowing.getBorrowDate());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getById_ShouldReturnBorrowing_WhenBorrowingExists() {
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));

        StepVerifier.create(borrowingService.getById(1L))
                .expectNextMatches(borrowing -> {
                    assertEquals(testBorrowing.getId(), borrowing.getId());
                    assertEquals(testBorrowing.getStatus(), borrowing.getStatus());
                    assertEquals(testUser.getId(), borrowing.getUserId());
                    assertEquals(testItem.getId(), borrowing.getItem().getId());
                    assertEquals(testBorrowing.getPurpose(), borrowing.getPurpose());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowEntityNotFoundException_WhenBorrowingNotFound() {
        when(borrowingRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(borrowingService.getById(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof EntityNotFoundException &&
                                throwable.getMessage().contains("Borrowing not found with id: 999")
                )
                .verify();

        verify(borrowingRepository).findById(999L);
    }

    @Test
    void activate_ShouldActivateBorrowing_WhenItemAvailable() {
        Borrowing pendingBorrowing = Borrowing.builder()
                .id(3L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.PENDING)
                .quantity(1)
                .borrowDate(null)
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();

        Borrowing activatedBorrowing = Borrowing.builder()
                .id(3L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();

        when(borrowingRepository.findById(3L)).thenReturn(Optional.of(pendingBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(activatedBorrowing);

        StepVerifier.create(borrowingService.activate(3L))
                .verifyComplete();

        verify(borrowingRepository).findById(3L);
        verify(borrowingRepository).save(argThat(borrowing ->
                borrowing.getStatus() == BorrowStatus.ACTIVE &&
                        borrowing.getBorrowDate() != null
        ));
    }

    @Test
    void activate_ShouldThrowIllegalStateException_WhenItemUnavailable() {
        Borrowing borrowingWithUnavailableItem = Borrowing.builder()
                .id(3L)
                .userId(testUser.getId())
                .item(testItemUnavailable)
                .status(BorrowStatus.PENDING)
                .quantity(1)
                .borrowDate(null)
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();

        when(borrowingRepository.findById(3L)).thenReturn(Optional.of(borrowingWithUnavailableItem));

        StepVerifier.create(borrowingService.activate(3L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Cannot activate borrowing - item is not available")
                )
                .verify();

        verify(borrowingRepository).findById(3L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void extend_ShouldExtendBorrowing_WhenValidConditions() {
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);
        Borrowing updatedBorrowing = Borrowing.builder()
                .id(1L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(2))
                .expectedReturnDate(newDueDate)
                .actualReturnDate(null)
                .purpose("Extended borrowing")
                .build();

        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(updatedBorrowing);

        StepVerifier.create(borrowingService.extend(1L, newDueDate))
                .expectNextMatches(borrowing -> {
                    assertEquals(newDueDate, borrowing.getExpectedReturnDate());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findById(1L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void extend_ShouldThrowIllegalArgumentException_WhenNewDueDateInPast() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        StepVerifier.create(borrowingService.extend(1L, pastDate))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("New due date must be in the future")
                )
                .verify();

        verify(borrowingRepository, never()).findById(anyLong());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void extend_ShouldThrowIllegalArgumentException_WhenNewDueDateBeforeCurrent() {
        // Arrange
        LocalDateTime earlierDate = testBorrowing.getExpectedReturnDate().minusDays(1);

        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));

        // Act & Assert
        StepVerifier.create(borrowingService.extend(1L, earlierDate))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("New due date must be after current expected return date")
                )
                .verify();

        verify(borrowingRepository).findById(1L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void extend_ShouldThrowIllegalStateException_WhenBorrowingNotActive() {
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);
        Borrowing returnedBorrowing = Borrowing.builder()
                .id(3L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.RETURNED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(5))
                .expectedReturnDate(LocalDateTime.now().minusDays(1))
                .actualReturnDate(LocalDateTime.now())
                .build();

        when(borrowingRepository.findById(3L)).thenReturn(Optional.of(returnedBorrowing));

        StepVerifier.create(borrowingService.extend(3L, newDueDate))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Only active borrowings can be extended")
                )
                .verify();

        verify(borrowingRepository).findById(3L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void returnBorrowing_ShouldReturnBorrowing_WhenActive() {
        Borrowing returnedBorrowing = Borrowing.builder()
                .id(1L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.RETURNED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(2))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .actualReturnDate(LocalDateTime.now())
                .purpose("Returned borrowing")
                .build();

        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(returnedBorrowing);

        StepVerifier.create(borrowingService.returnBorrowing(1L))
                .expectNextMatches(borrowing -> {
                    assertEquals(BorrowStatus.RETURNED, borrowing.getStatus());
                    assertNotNull(borrowing.getActualReturnDate());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findById(1L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void returnBorrowing_ShouldReturnBorrowing_WhenOverdue() {
        // Arrange
        testOverdueBorrowing.setStatus(BorrowStatus.OVERDUE);
        Borrowing returnedBorrowing = Borrowing.builder()
                .id(2L)
                .userId(testUser2.getId())
                .item(testItem)
                .status(BorrowStatus.RETURNED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(10))
                .expectedReturnDate(LocalDateTime.now().minusDays(2))
                .actualReturnDate(LocalDateTime.now())
                .purpose("Returned overdue borrowing")
                .build();

        when(borrowingRepository.findById(2L)).thenReturn(Optional.of(testOverdueBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(returnedBorrowing);

        // Act & Assert
        StepVerifier.create(borrowingService.returnBorrowing(2L))
                .expectNextMatches(borrowing -> {
                    assertEquals(BorrowStatus.RETURNED, borrowing.getStatus());
                    assertNotNull(borrowing.getActualReturnDate());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findById(2L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void returnBorrowing_ShouldThrowIllegalStateException_WhenBorrowingNotActiveOrOverdue() {
        Borrowing cancelledBorrowing = Borrowing.builder()
                .id(3L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.CANCELLED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(1))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();

        when(borrowingRepository.findById(3L)).thenReturn(Optional.of(cancelledBorrowing));

        StepVerifier.create(borrowingService.returnBorrowing(3L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Only active or overdue borrowings can be returned")
                )
                .verify();

        verify(borrowingRepository).findById(3L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void cancel_ShouldCancelBorrowing_WhenActive() {
        Borrowing cancelledBorrowing = Borrowing.builder()
                .id(1L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.CANCELLED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(2))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .actualReturnDate(null)
                .purpose("Cancelled borrowing")
                .build();

        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(cancelledBorrowing);

        StepVerifier.create(borrowingService.cancel(1L))
                .verifyComplete();

        verify(borrowingRepository).findById(1L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void cancel_ShouldThrowIllegalStateException_WhenBorrowingNotActive() {
        Borrowing returnedBorrowing = Borrowing.builder()
                .id(3L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.RETURNED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(5))
                .expectedReturnDate(LocalDateTime.now().minusDays(1))
                .actualReturnDate(LocalDateTime.now())
                .build();

        when(borrowingRepository.findById(3L)).thenReturn(Optional.of(returnedBorrowing));

        StepVerifier.create(borrowingService.cancel(3L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Only pending borrowings can be canceled")
                )
                .verify();

        verify(borrowingRepository).findById(3L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void findBorrowingsByFilters_ShouldReturnFilteredResults() {
        List<Borrowing> borrowings = List.of(testBorrowing);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("borrowDate").descending());
        Page<Borrowing> page = new PageImpl<>(borrowings, pageable, borrowings.size());

        when(borrowingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        StepVerifier.create(borrowingService.findBorrowingsByFilters(
                        BorrowStatus.ACTIVE, 1L, 1L,
                        LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(5), pageable
                ))
                .expectNextMatches(borrowing -> {
                    assertEquals(BorrowStatus.ACTIVE, borrowing.getStatus());
                    assertEquals(testUser.getId(), borrowing.getUserId());
                    assertEquals(testItem.getId(), borrowing.getItem().getId());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findOverdueBorrowings_ShouldReturnOverdueBorrowingsAndUpdateStatus() {
        List<Borrowing> borrowings = List.of(testOverdueBorrowing);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> page = new PageImpl<>(borrowings, pageable, borrowings.size());

        when(borrowingRepository.findOverdueBorrowings(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(page);
        when(borrowingRepository.saveAll(anyList())).thenReturn(List.of(testOverdueBorrowing));

        StepVerifier.create(borrowingService.findOverdueBorrowings(pageable))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<List<Borrowing>> captor = ArgumentCaptor.forClass(List.class);
        verify(borrowingRepository).saveAll(captor.capture());

        List<Borrowing> savedBorrowings = captor.getValue();
        assertNotNull(savedBorrowings);
        assertTrue(savedBorrowings.stream().allMatch(b -> b.getStatus() == BorrowStatus.OVERDUE));
    }

    @Test
    void countOverdueBorrowings_ShouldReturnCorrectCount() {
        when(borrowingRepository.countOverdueBorrowings(any(LocalDateTime.class))).thenReturn(2L);

        StepVerifier.create(borrowingService.countOverdueBorrowings())
                .expectNextMatches(count -> count == 2L)
                .verifyComplete();

        verify(borrowingRepository).countOverdueBorrowings(any(LocalDateTime.class));
    }

    @Test
    void countBorrowingsByFilters_ShouldReturnCorrectCount() {
        when(borrowingRepository.count(any(Specification.class))).thenReturn(5L);

        StepVerifier.create(borrowingService.countBorrowingsByFilters(
                        BorrowStatus.ACTIVE, 1L, 1L,
                        LocalDateTime.now().minusDays(30), LocalDateTime.now()
                ))
                .expectNextMatches(count -> count == 5L)
                .verifyComplete();

        verify(borrowingRepository).count(any(Specification.class));
    }

    @Test
    void create_ShouldHandleNullQuantityInDTO() {
        BorrowingDTO dtoWithoutQuantity = new BorrowingDTO(
                null,
                1L,
                1L,
                null,
                null,
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "Test purpose"
        );

        Borrowing entity = Borrowing.builder()
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .purpose("Test purpose")
                .build();

        Borrowing savedBorrowing = Borrowing.builder()
                .id(5L)
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .status(BorrowStatus.ACTIVE)
                .purpose("Test purpose")
                .build();

        when(mapper.toEntity(dtoWithoutQuantity)).thenReturn(entity);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem));
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));
        when(borrowingRepository.countActiveBorrowingsByUser(1L)).thenReturn(2L);
        when(borrowingRepository.save(entity)).thenReturn(savedBorrowing);

        StepVerifier.create(borrowingService.create(dtoWithoutQuantity))
                .expectNextMatches(borrowing -> {
                    assertEquals(1, borrowing.getQuantity());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void create_ShouldHandleNullBorrowDateInDTO() {
        BorrowingDTO dtoWithoutBorrowDate = new BorrowingDTO(
                null,
                1L,
                1L,
                1,
                null,
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "Test purpose"
        );

        Borrowing entity = Borrowing.builder()
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .purpose("Test purpose")
                .build();

        Borrowing savedBorrowing = Borrowing.builder()
                .id(6L)
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .status(BorrowStatus.ACTIVE)
                .purpose("Test purpose")
                .build();

        when(mapper.toEntity(dtoWithoutBorrowDate)).thenReturn(entity);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem));
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));
        when(borrowingRepository.countActiveBorrowingsByUser(1L)).thenReturn(2L);
        when(borrowingRepository.save(entity)).thenReturn(savedBorrowing);

        StepVerifier.create(borrowingService.create(dtoWithoutBorrowDate))
                .expectNextMatches(borrowing -> {
                    assertNotNull(borrowing.getBorrowDate());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void create_ShouldThrowException_WhenItemServiceReturnsError() {
        Borrowing entity = Borrowing.builder()
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .purpose("Test purpose")
                .build();

        BorrowingDTO dto = new BorrowingDTO(
                null, 1L, 1L, 1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(7), null, BorrowStatus.ACTIVE, "Test purpose"
        );

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(itemService.getById(1L)).thenReturn(Mono.error(new RuntimeException("Item not found")));

        StepVerifier.create(borrowingService.create(dto))
                .expectError(RuntimeException.class)
                .verify();

        verify(mapper).toEntity(dto);
        verify(itemService).getById(1L);
        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    void create_ShouldThrowException_WhenUserServiceReturnsError() {
        Borrowing entity = Borrowing.builder()
                .item(testItem)
                .userId(testUser.getId())
                .quantity(1)
                .expectedReturnDate(LocalDateTime.now().plusDays(7))
                .purpose("Test purpose")
                .build();

        BorrowingDTO dto = new BorrowingDTO(
                null, 1L, 1L, 1, LocalDateTime.now(),
                LocalDateTime.now().plusDays(7), null, BorrowStatus.ACTIVE, "Test purpose"
        );

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem));
        when(userService.getUserById(1L)).thenReturn(Mono.error(new RuntimeException("User not found")));

        StepVerifier.create(borrowingService.create(dto))
                .expectError(RuntimeException.class)
                .verify();

        verify(mapper).toEntity(dto);
        verify(itemService).getById(1L);
        verify(userService).getUserById(1L);
    }


    @Test
    void activate_ShouldThrowEntityNotFoundException_WhenBorrowingNotFound() {
        when(borrowingRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(borrowingService.activate(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof EntityNotFoundException &&
                                throwable.getMessage().contains("Borrowing not found with id: 999")
                )
                .verify();

        verify(borrowingRepository).findById(999L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void activate_ShouldThrowIllegalStateException_WhenItemNeedsMaintenance() {
        Borrowing borrowingWithBadItem = Borrowing.builder()
                .id(6L)
                .userId(testUser.getId())
                .item(testItemNeedsMaintenance)
                .status(BorrowStatus.PENDING)
                .quantity(1)
                .borrowDate(null)
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();

        when(borrowingRepository.findById(6L)).thenReturn(Optional.of(borrowingWithBadItem));

        StepVerifier.create(borrowingService.activate(6L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Cannot activate borrowing - item is not available: NEEDS_MAINTENANCE")
                )
                .verify();

        verify(borrowingRepository).findById(6L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void activate_ShouldThrowIllegalStateException_WhenItemDecommissioned() {
        Borrowing borrowingWithBadItem = Borrowing.builder()
                .id(7L)
                .userId(testUser.getId())
                .item(testItemDecommissioned)
                .status(BorrowStatus.PENDING)
                .quantity(1)
                .borrowDate(null)
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();

        when(borrowingRepository.findById(7L)).thenReturn(Optional.of(borrowingWithBadItem));

        StepVerifier.create(borrowingService.activate(7L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Cannot activate borrowing - item is not available: DECOMMISSIONED")
                )
                .verify();

        verify(borrowingRepository).findById(7L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void activate_ShouldWork_WhenBorrowingAlreadyActive() {
        Borrowing alreadyActiveBorrowing = Borrowing.builder()
                .id(8L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(1))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();

        when(borrowingRepository.findById(8L)).thenReturn(Optional.of(alreadyActiveBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(alreadyActiveBorrowing);

        StepVerifier.create(borrowingService.activate(8L))
                .verifyComplete();

        verify(borrowingRepository).findById(8L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }


    @Test
    void extend_ShouldThrowEntityNotFoundException_WhenBorrowingNotFound() {
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);
        when(borrowingRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(borrowingService.extend(999L, newDueDate))
                .expectErrorMatches(throwable ->
                        throwable instanceof EntityNotFoundException &&
                                throwable.getMessage().contains("Borrowing not found with id: 999")
                )
                .verify();

        verify(borrowingRepository).findById(999L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void extend_ShouldThrowIllegalStateException_WhenBorrowingCancelled() {
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);
        when(borrowingRepository.findById(4L)).thenReturn(Optional.of(testBorrowingCancelled));

        StepVerifier.create(borrowingService.extend(4L, newDueDate))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Only active borrowings can be extended")
                )
                .verify();

        verify(borrowingRepository).findById(4L);
        verify(borrowingRepository, never()).save(any());
    }


    @Test
    void returnBorrowing_ShouldThrowEntityNotFoundException_WhenBorrowingNotFound() {
        when(borrowingRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(borrowingService.returnBorrowing(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof EntityNotFoundException &&
                                throwable.getMessage().contains("Borrowing not found with id: 999")
                )
                .verify();

        verify(borrowingRepository).findById(999L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void returnBorrowing_ShouldLogWarning_WhenReturnedLate() {
        Borrowing lateBorrowing = Borrowing.builder()
                .id(9L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(10))
                .expectedReturnDate(LocalDateTime.now().minusDays(2))
                .actualReturnDate(null)
                .purpose("Late borrowing")
                .build();

        Borrowing returnedBorrowing = Borrowing.builder()
                .id(9L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.RETURNED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(10))
                .expectedReturnDate(LocalDateTime.now().minusDays(2))
                .actualReturnDate(LocalDateTime.now())
                .purpose("Late borrowing")
                .build();

        when(borrowingRepository.findById(9L)).thenReturn(Optional.of(lateBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(returnedBorrowing);

        StepVerifier.create(borrowingService.returnBorrowing(9L))
                .expectNextMatches(borrowing -> {
                    assertEquals(BorrowStatus.RETURNED, borrowing.getStatus());
                    assertNotNull(borrowing.getActualReturnDate());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findById(9L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }


    @Test
    void cancel_ShouldThrowEntityNotFoundException_WhenBorrowingNotFound() {
        when(borrowingRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(borrowingService.cancel(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof EntityNotFoundException &&
                                throwable.getMessage().contains("Borrowing not found with id: 999")
                )
                .verify();

        verify(borrowingRepository).findById(999L);
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void findBorrowingsByFilters_ShouldReturnEmpty_WhenNoResults() {
        List<Borrowing> emptyList = List.of();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("borrowDate").descending());
        Page<Borrowing> page = new PageImpl<>(emptyList, pageable, 0);

        when(borrowingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        StepVerifier.create(borrowingService.findBorrowingsByFilters(
                        BorrowStatus.CANCELLED, 999L, 999L,
                        LocalDateTime.now().minusDays(30), LocalDateTime.now(), pageable
                ))
                .verifyComplete();

        verify(borrowingRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findBorrowingsByFilters_ShouldWork_WithOnlyFromDate() {
        List<Borrowing> borrowings = List.of(testBorrowing);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> page = new PageImpl<>(borrowings, pageable, borrowings.size());

        when(borrowingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        StepVerifier.create(borrowingService.findBorrowingsByFilters(
                        null, null, null,
                        LocalDateTime.now().minusDays(5), null, pageable
                ))
                .expectNextCount(1)
                .verifyComplete();

        verify(borrowingRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findBorrowingsByFilters_ShouldWork_WithOnlyToDate() {
        List<Borrowing> borrowings = List.of(testBorrowing);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> page = new PageImpl<>(borrowings, pageable, borrowings.size());

        when(borrowingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        StepVerifier.create(borrowingService.findBorrowingsByFilters(
                        null, null, null,
                        null, LocalDateTime.now().plusDays(5), pageable
                ))
                .expectNextCount(1)
                .verifyComplete();

        verify(borrowingRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void countBorrowingsByFilters_ShouldReturnZero_WhenNoResults() {
        when(borrowingRepository.count(any(Specification.class))).thenReturn(0L);

        StepVerifier.create(borrowingService.countBorrowingsByFilters(
                        BorrowStatus.CANCELLED, 999L, 999L,
                        LocalDateTime.now().minusDays(30), LocalDateTime.now()
                ))
                .expectNext(0L)
                .verifyComplete();

        verify(borrowingRepository).count(any(Specification.class));
    }

    @Test
    void findOverdueBorrowings_ShouldReturnEmpty_WhenNoOverdue() {
        List<Borrowing> emptyList = List.of();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> page = new PageImpl<>(emptyList, pageable, 0);

        when(borrowingRepository.findOverdueBorrowings(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(page);

        StepVerifier.create(borrowingService.findOverdueBorrowings(pageable))
                .verifyComplete();

        verify(borrowingRepository).findOverdueBorrowings(any(LocalDateTime.class), eq(pageable));
        verify(borrowingRepository, never()).saveAll(any());
    }

    @Test
    void findOverdueBorrowings_ShouldNotSave_WhenAlreadyOverdue() {
        Borrowing alreadyOverdue = Borrowing.builder()
                .id(10L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.OVERDUE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(10))
                .expectedReturnDate(LocalDateTime.now().minusDays(2))
                .actualReturnDate(null)
                .purpose("Already overdue")
                .build();

        List<Borrowing> borrowings = List.of(alreadyOverdue);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> page = new PageImpl<>(borrowings, pageable, borrowings.size());

        when(borrowingRepository.findOverdueBorrowings(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(page);

        StepVerifier.create(borrowingService.findOverdueBorrowings(pageable))
                .expectNextCount(1)
                .verifyComplete();

        verify(borrowingRepository).findOverdueBorrowings(any(LocalDateTime.class), eq(pageable));
        verify(borrowingRepository, never()).saveAll(any());
    }

    @Test
    void countOverdueBorrowings_ShouldReturnZero_WhenNone() {
        when(borrowingRepository.countOverdueBorrowings(any(LocalDateTime.class))).thenReturn(0L);

        StepVerifier.create(borrowingService.countOverdueBorrowings())
                .expectNext(0L)
                .verifyComplete();

        verify(borrowingRepository).countOverdueBorrowings(any(LocalDateTime.class));
    }



    @Test
    void extend_ShouldWork_WhenNewDueDateEqualsCurrent() {
        LocalDateTime newDueDate = testBorrowing.getExpectedReturnDate();

        Borrowing updatedBorrowing = Borrowing.builder()
                .id(1L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(2))
                .expectedReturnDate(newDueDate)
                .actualReturnDate(null)
                .purpose("Extended borrowing")
                .build();

        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(updatedBorrowing);

        StepVerifier.create(borrowingService.extend(1L, newDueDate))
                .expectNextMatches(borrowing -> {
                    assertEquals(newDueDate, borrowing.getExpectedReturnDate());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findById(1L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void returnBorrowing_ShouldNotSetActualReturnDate_WhenAlreadySet() {
        LocalDateTime existingReturnDate = LocalDateTime.now().minusHours(2);
        Borrowing borrowingWithReturnDate = Borrowing.builder()
                .id(12L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(5))
                .expectedReturnDate(LocalDateTime.now().plusDays(2))
                .actualReturnDate(existingReturnDate)
                .purpose("Already has return date")
                .build();

        Borrowing returnedBorrowing = Borrowing.builder()
                .id(12L)
                .userId(testUser.getId())
                .item(testItem)
                .status(BorrowStatus.RETURNED)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(5))
                .expectedReturnDate(LocalDateTime.now().plusDays(2))
                .actualReturnDate(existingReturnDate)
                .purpose("Already has return date")
                .build();

        when(borrowingRepository.findById(12L)).thenReturn(Optional.of(borrowingWithReturnDate));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(returnedBorrowing);

        StepVerifier.create(borrowingService.returnBorrowing(12L))
                .expectNextMatches(borrowing -> {
                    assertEquals(BorrowStatus.RETURNED, borrowing.getStatus());
                    assertEquals(existingReturnDate, borrowing.getActualReturnDate());
                    return true;
                })
                .verifyComplete();

        verify(borrowingRepository).findById(12L);
        verify(borrowingRepository).save(any(Borrowing.class));
    }



    @Test
    void borrowingMapper_ShouldMapEntityToDTO() {
        Borrowing borrowing = testBorrowing;

        BorrowingDTO dto = new BorrowingDTO(borrowing);

        assertEquals(borrowing.getId(), dto.id());
        assertEquals(borrowing.getItem().getId(), dto.itemId());
        assertEquals(borrowing.getUserId(), dto.userId());
        assertEquals(borrowing.getQuantity(), dto.quantity());
        assertEquals(borrowing.getBorrowDate(), dto.borrowDate());
        assertEquals(borrowing.getExpectedReturnDate(), dto.expectedReturnDate());
        assertEquals(borrowing.getActualReturnDate(), dto.actualReturnDate());
        assertEquals(borrowing.getStatus(), dto.status());
        assertEquals(borrowing.getPurpose(), dto.purpose());
    }

    @Test
    void borrowingMapper_ShouldHandleNullEntity() {
        BorrowingDTO dto = new BorrowingDTO(null);

        assertNull(dto.id());
        assertNull(dto.itemId());
        assertNull(dto.userId());
        assertEquals(1, dto.quantity());
        assertNotNull(dto.borrowDate());
        assertNull(dto.expectedReturnDate());
        assertNull(dto.actualReturnDate());
        assertEquals(BorrowStatus.ACTIVE, dto.status());
        assertNull(dto.purpose());
    }
}