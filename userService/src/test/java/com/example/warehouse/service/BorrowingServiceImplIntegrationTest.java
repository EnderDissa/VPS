package com.example.warehouse.service;

import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.BorrowStatus;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.repository.BorrowingRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest

@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BorrowingServiceImplIntegrationTest {

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
    private BorrowingServiceImpl borrowingService;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testUser2;
    private  User nonExistentUser;
    private Item testItem;
    private Item nonExistentItem;
    private Item testItemUnavailable;
    private Borrowing testBorrowing;
    private Borrowing testOverdueBorrowing;

    @BeforeEach
    void setUp() {
        borrowingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@example.com")
                .firstName("John")
                .secondName("Johnovic")
                .lastName("Doe")
                .role(RoleType.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        testUser2 = User.builder()
                .email("test2@example.com")
                .firstName("Jane")
                .secondName("Johnovic")
                .lastName("Smith")
                .role(RoleType.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();
        testUser2 = userRepository.save(testUser2);

        nonExistentUser = User.builder()
                .id(999L)
                .email("test2@example.com")
                .firstName("Jane")
                .secondName("Johnovic")
                .lastName("Smith")
                .role(RoleType.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();

        testItem = Item.builder()
                .name("Test Laptop")
                .description("High-performance laptop")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("1")
                .description("good")
                .createdAt(LocalDateTime.now())
                .build();
        testItem = itemService.create(testItem);

        nonExistentItem = Item.builder()
                .id(999L)
                .name("Test Laptop")
                .description("High-performance laptop")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .serialNumber("1")
                .description("good")
                .createdAt(LocalDateTime.now())
                .build();

        testItemUnavailable = Item.builder()
                .name("Broken Monitor")
                .description("Needs repair")
                .type(ItemType.ELECTRONICS)
                .condition(ItemCondition.UNDER_REPAIR)
                .serialNumber("2")
                .description("good")
                .createdAt(LocalDateTime.now())
                .build();
        testItemUnavailable = itemRepository.save(testItemUnavailable);

        testBorrowing = Borrowing.builder()
                .user(testUser)
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(2))
                .expectedReturnDate(LocalDateTime.now().plusDays(5))
                .build();
        testBorrowing = borrowingRepository.save(testBorrowing);

        testOverdueBorrowing = Borrowing.builder()
                .user(testUser2)
                .item(testItem)
                .status(BorrowStatus.ACTIVE)
                .quantity(1)
                .borrowDate(LocalDateTime.now().minusDays(10))
                .expectedReturnDate(LocalDateTime.now().minusDays(2))
                .build();
        testOverdueBorrowing = borrowingRepository.save(testOverdueBorrowing);
    }

    @Test
    void getById_ShouldReturnBorrowing_WhenBorrowingExists() {
        Borrowing result = borrowingService.getById(testBorrowing.getId());

        assertNotNull(result);
        assertEquals(testBorrowing.getId(), result.getId());
        assertEquals(testBorrowing.getStatus(), result.getStatus());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(testItem.getId(), result.getItem().getId());
    }

    @Test
    void getById_ShouldThrowException_WhenBorrowingNotFound() {
        Long nonExistentId = 999L;

        assertThrows(EntityNotFoundException.class, () -> borrowingService.getById(nonExistentId));
    }

    @Test
    void create_ShouldCreateBorrowing_WhenValidData() {
        Borrowing newBorrowing = new Borrowing(
                null,
                testItem,
                testUser2,
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "a"
        );

        Borrowing result = borrowingService.create(newBorrowing);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(BorrowStatus.ACTIVE, result.getStatus());
        assertEquals(testUser2.getId(), result.getUser().getId());
        assertEquals(testItem.getId(), result.getItem().getId());

        Borrowing savedBorrowing = borrowingRepository.findById(result.getId()).orElseThrow();
        assertEquals(BorrowStatus.ACTIVE, savedBorrowing.getStatus());
    }

    @Test
    void create_ShouldThrowException_WhenItemNotFound() {
        Borrowing newBorrowing = new Borrowing(
                null,
                nonExistentItem,
                testUser,
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "a"
        );

        assertThrows(ItemNotFoundException.class, () -> borrowingService.create(newBorrowing));
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        Borrowing newBorrowing = new Borrowing(
                null,
                testItem,
                nonExistentUser,
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "a"
        );

        assertThrows(UserNotFoundException.class, () -> borrowingService.create(newBorrowing));
    }

    @Test
    void create_ShouldThrowException_WhenItemUnavailable() {
        Borrowing newBorrowing = new Borrowing(
                null,
                testItemUnavailable,
                testUser,
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "a"
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> borrowingService.create(newBorrowing));
        assertTrue(exception.getMessage().contains("Cannot borrow item in condition"));
    }

    @Test
    void create_ShouldThrowException_WhenUserExceedsBorrowingLimit() {
        for (int i = 0; i < 5; i++) {
            Item additionalItem = Item.builder()
                    .name("Item " + i)
                    .description("Test item " + i)
                    .type(ItemType.ELECTRONICS)
                    .condition(ItemCondition.GOOD)
                    .serialNumber(UUID.randomUUID().toString())
                    .description("good")
                    .createdAt(LocalDateTime.now())
                    .build();
            additionalItem = itemRepository.save(additionalItem);

            Borrowing borrowing = Borrowing.builder()
                    .user(testUser)
                    .item(additionalItem)
                    .status(BorrowStatus.ACTIVE)
                    .quantity(1)
                    .borrowDate(LocalDateTime.now().minusDays(1))
                    .expectedReturnDate(LocalDateTime.now().plusDays(5))
                    .build();
            borrowingRepository.save(borrowing);
        }

        Borrowing newBorrowing = new Borrowing(
                null,
                testItem,
                testUser,
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                BorrowStatus.ACTIVE,
                "a"
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> borrowingService.create(newBorrowing));
        assertTrue(exception.getMessage().contains("maximum active borrowings limit"));
    }

    @Test
    void activate_ShouldThrowException_WhenBorrowingNotFound() {
        Long nonExistentId = 999L;

        assertThrows(EntityNotFoundException.class, () -> borrowingService.activate(nonExistentId));
    }

    @Test
    void extend_ShouldExtendBorrowing_WhenValidConditions() {
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);

        Borrowing result = borrowingService.extend(testBorrowing.getId(), newDueDate);

        assertEquals(newDueDate.withNano(0), result.getExpectedReturnDate().withNano(0));

        Borrowing updatedBorrowing = borrowingRepository.findById(testBorrowing.getId()).orElseThrow();
        assertEquals(result.getExpectedReturnDate().withNano(0),
                updatedBorrowing.getExpectedReturnDate().withNano(0));
    }

    @Test
    void extend_ShouldThrowException_WhenNewDueDateInPast() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> borrowingService.extend(testBorrowing.getId(), pastDate));
        assertTrue(exception.getMessage().contains("New due date must be in the future"));
    }

    @Test
    void extend_ShouldThrowException_WhenNewDueDateBeforeCurrent() {
        LocalDateTime earlierDate = testBorrowing.getExpectedReturnDate().minusDays(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> borrowingService.extend(testBorrowing.getId(), earlierDate));
        assertTrue(exception.getMessage().contains("New due date must be after current expected return date"));
    }

    @Test
    void extend_ShouldThrowException_WhenBorrowingNotActive() {
        Borrowing returnedBorrowing = borrowingRepository.save(
                Borrowing.builder()
                    .user(testUser)
                    .item(testItem)
                    .status(BorrowStatus.RETURNED)
                    .quantity(1)
                    .borrowDate(LocalDateTime.now().minusDays(5))
                    .expectedReturnDate(LocalDateTime.now().minusDays(1))
                    .actualReturnDate(LocalDateTime.now())
                    .build()
        );

        LocalDateTime newDueDate = LocalDateTime.now().plusDays(10);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> borrowingService.extend(returnedBorrowing.getId(), newDueDate));
        assertTrue(exception.getMessage().contains("Only active borrowings can be extended"));
    }

    @Test
    void returnBorrowing_ShouldReturnBorrowing_WhenActive() {
        Borrowing result = borrowingService.returnBorrowing(testBorrowing.getId());

        assertEquals(BorrowStatus.RETURNED, result.getStatus());
        assertNotNull(result.getActualReturnDate());

        Borrowing updatedBorrowing = borrowingRepository.findById(testBorrowing.getId()).orElseThrow();
        assertEquals(BorrowStatus.RETURNED, updatedBorrowing.getStatus());
        assertNotNull(updatedBorrowing.getActualReturnDate());
    }

    @Test
    void returnBorrowing_ShouldReturnBorrowing_WhenOverdue() {
        testOverdueBorrowing.setStatus(BorrowStatus.OVERDUE);
        borrowingRepository.save(testOverdueBorrowing);

        Borrowing result = borrowingService.returnBorrowing(testOverdueBorrowing.getId());

        assertEquals(BorrowStatus.RETURNED, result.getStatus());
    }

    @Test
    void returnBorrowing_ShouldThrowException_WhenBorrowingNotActiveOrOverdue() {
        Borrowing cancelledBorrowing = borrowingRepository.save(
                Borrowing.builder()
                        .user(testUser)
                        .item(testItem)
                        .status(BorrowStatus.CANCELLED)
                        .quantity(1)
                        .borrowDate(LocalDateTime.now().minusDays(1))
                        .expectedReturnDate(LocalDateTime.now().plusDays(5))
                        .build()
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> borrowingService.returnBorrowing(cancelledBorrowing.getId()));
        assertTrue(exception.getMessage().contains("Only active or overdue borrowings can be returned"));
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithAllFilters() {
        Page<Borrowing> result = borrowingService.findPage(
                0, 10, BorrowStatus.ACTIVE, testUser.getId(), testItem.getId(),
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(5)
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(1, result.getContent().size());

        Borrowing dto = result.getContent().get(0);
        assertEquals(BorrowStatus.ACTIVE, dto.getStatus());
        assertEquals(testUser.getId(), dto.getUser().getId());
        assertEquals(testItem.getId(), dto.getItem().getId());
    }

    @Test
    void findPage_ShouldReturnEmpty_WhenNoMatches() {
        Page<Borrowing> result = borrowingService.findPage(
                0, 10, BorrowStatus.CANCELLED, null, null, null, null
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findPage_ShouldReturnPagedResults() {
        for (int i = 0; i < 5; i++) {
            Borrowing borrowing = Borrowing.builder()
                    .user(testUser)
                    .item(testItem)
                    .status(BorrowStatus.ACTIVE)
                    .quantity(1)
                    .borrowDate(LocalDateTime.now().minusDays(i))
                    .expectedReturnDate(LocalDateTime.now().plusDays(5))
                    .build();
            borrowingRepository.save(borrowing);
        }

        Page<Borrowing> result = borrowingService.findPage(0, 3, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertTrue(result.getTotalElements() >= 7);
    }

    @Test
    void findOverdue_ShouldReturnOverdueBorrowings() {
        Page<Borrowing> result = borrowingService.findOverdue(0, 10);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);

        List<Borrowing> overdueBorrowings = result.getContent();
        assertFalse(overdueBorrowings.isEmpty());

        boolean foundOverdue = overdueBorrowings.stream()
                .anyMatch(b -> b.getId().equals(testOverdueBorrowing.getId()));
        assertTrue(foundOverdue);
    }

    @Test
    void findOverdue_ShouldUpdateStatusToOverdue() {
        assertEquals(BorrowStatus.ACTIVE, testOverdueBorrowing.getStatus());

        Page<Borrowing> result = borrowingService.findOverdue(0, 10);

        Borrowing updatedBorrowing = borrowingRepository.findById(testOverdueBorrowing.getId()).orElseThrow();
        assertEquals(BorrowStatus.OVERDUE, updatedBorrowing.getStatus());
    }

    @Test
    void updateOverdueBorrowings_ShouldUpdateActiveToOverdue() {
        assertEquals(BorrowStatus.ACTIVE, testOverdueBorrowing.getStatus());

        borrowingService.updateOverdueBorrowings();

        Borrowing updatedBorrowing = borrowingRepository.findById(testOverdueBorrowing.getId()).orElseThrow();
        assertEquals(BorrowStatus.OVERDUE, updatedBorrowing.getStatus());

        Borrowing nonOverdueBorrowing = borrowingRepository.findById(testBorrowing.getId()).orElseThrow();
        assertEquals(BorrowStatus.ACTIVE, nonOverdueBorrowing.getStatus());
    }
}
