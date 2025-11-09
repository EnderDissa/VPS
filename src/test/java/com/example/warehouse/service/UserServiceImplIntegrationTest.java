package com.example.warehouse.service;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class UserServiceImplIntegrationTest {

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
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User testUser4;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = User.builder()
                .firstName("John")
                .secondName("Michael")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(RoleType.ADMIN)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        testUser2 = User.builder()
                .firstName("Jane")
                .secondName("Marie")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .role(RoleType.STUDENT)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        testUser3 = User.builder()
                .firstName("Bob")
                .secondName(null)
                .lastName("Johnson")
                .email("bob.johnson@example.com")
                .role(RoleType.DRIVER)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        testUser4 = User.builder()
                .firstName("Alice")
                .secondName("Ann")
                .lastName("Williams")
                .email("alice.williams@example.com")
                .role(RoleType.STUDENT)
                .createdAt(LocalDateTime.now().minusHours(12))
                .build();

        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
        testUser4 = userRepository.save(testUser4);
    }

    @Test
    void createUser_ShouldCreateUser_WhenValidData() {
        UserRequestDTO newUserRequest = new UserRequestDTO(
                null,
                "New",
                "Middle",
                "User",
                RoleType.STUDENT,
                "new.user@example.com",
                null
        );

        UserResponseDTO result = userService.createUser(newUserRequest);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("New", result.firstName());
        assertEquals("Middle", result.secondName());
        assertEquals("User", result.lastName());
        assertEquals(RoleType.STUDENT, result.role());
        assertEquals("new.user@example.com", result.email());
        assertNotNull(result.createdAt());

        User savedUser = userRepository.findById(result.id()).orElseThrow();
        assertEquals("New", savedUser.getFirstName());
        assertEquals("Middle", savedUser.getSecondName());
        assertEquals("User", savedUser.getLastName());
        assertEquals("new.user@example.com", savedUser.getEmail());
    }

    @Test
    void createUser_ShouldCreateUser_WhenSecondNameIsNull() {
        UserRequestDTO newUserRequest = new UserRequestDTO(
                null,
                "NoMiddle",
                null,
                "Name",
                RoleType.DRIVER,
                "nomiddle.name@example.com",
                null
        );

        UserResponseDTO result = userService.createUser(newUserRequest);

        assertNotNull(result);
        assertNull(result.secondName());
        assertEquals("NoMiddle", result.firstName());
        assertEquals("Name", result.lastName());
    }

    @Test
    void createUser_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        UserRequestDTO duplicateUserRequest = new UserRequestDTO(
                null,
                "Different",
                "Name",
                "User",
                RoleType.STUDENT,
                "john.doe@example.com",
                null
        );

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(duplicateUserRequest)
        );

        assertTrue(exception.getMessage().contains("User with email john.doe@example.com already exists"));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        User result = userService.getUserById(testUser1.getId());

        assertNotNull(result);
        assertEquals(testUser1.getId(), result.getId());
        assertEquals(testUser1.getFirstName(), result.getFirstName());
        assertEquals(testUser1.getSecondName(), result.getSecondName());
        assertEquals(testUser1.getLastName(), result.getLastName());
        assertEquals(testUser1.getEmail(), result.getEmail());
        assertEquals(testUser1.getRole(), result.getRole());
        assertEquals(testUser1.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long nonExistentId = 999L;

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentId));
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        UserResponseDTO result = userService.getUserByEmail("jane.smith@example.com");

        assertNotNull(result);
        assertEquals(testUser2.getId(), result.id());
        assertEquals(testUser2.getFirstName(), result.firstName());
        assertEquals(testUser2.getLastName(), result.lastName());
        assertEquals("jane.smith@example.com", result.email());
    }

    @Test
    void getUserByEmail_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        String nonExistentEmail = "nonexistent@example.com";

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByEmail(nonExistentEmail)
        );

        assertTrue(exception.getMessage().contains("User not found with email: " + nonExistentEmail));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<UserResponseDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(4, result.size());

        assertTrue(result.stream().anyMatch(user -> user.email().equals("john.doe@example.com")));
        assertTrue(result.stream().anyMatch(user -> user.email().equals("jane.smith@example.com")));
        assertTrue(result.stream().anyMatch(user -> user.email().equals("bob.johnson@example.com")));
        assertTrue(result.stream().anyMatch(user -> user.email().equals("alice.williams@example.com")));
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        userRepository.deleteAll();

        List<UserResponseDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByRole_ShouldReturnUsers_WhenRoleExists() {
        List<UserResponseDTO> result = userService.getUsersByRole(RoleType.STUDENT);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(user -> user.role() == RoleType.STUDENT));

        assertTrue(result.stream().anyMatch(user -> user.email().equals("jane.smith@example.com")));
        assertTrue(result.stream().anyMatch(user -> user.email().equals("alice.williams@example.com")));
    }

    @Test
    void getUsersByRole_ShouldReturnEmptyList_WhenNoUsersWithRole() {
        List<UserResponseDTO> result = userService.getUsersByRole(RoleType.MANAGER);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchUsersByLastName_ShouldReturnUsers_WhenLastNameMatches() {
        List<UserResponseDTO> result = userService.searchUsersByLastName("smith");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).firstName());
        assertEquals("Smith", result.get(0).lastName());
    }

    @Test
    void searchUsersByLastName_ShouldReturnUsers_WhenCaseInsensitive() {
        List<UserResponseDTO> result = userService.searchUsersByLastName("SMITH");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Smith", result.get(0).lastName());
    }

    @Test
    void searchUsersByLastName_ShouldReturnUsers_WhenPartialMatch() {
        List<UserResponseDTO> result = userService.searchUsersByLastName("son");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Johnson", result.get(0).lastName());
    }

    @Test
    void searchUsersByLastName_ShouldReturnMultipleUsers_WhenMultipleMatches() {
        User anotherSmith = User.builder()
                .firstName("Tom")
                .lastName("Smith")
                .email("tom.smith@example.com")
                .role(RoleType.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(anotherSmith);

        List<UserResponseDTO> result = userService.searchUsersByLastName("Smith");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(user -> user.lastName().equals("Smith")));
    }

    @Test
    void searchUsersByLastName_ShouldReturnEmptyList_WhenNoMatches() {
        List<UserResponseDTO> result = userService.searchUsersByLastName("Nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenValidData() {
        UserRequestDTO updateRequest = new UserRequestDTO(
                testUser1.getId(),
                "JohnUpdated",
                "MichaelUpdated",
                "DoeUpdated",
                RoleType.MANAGER,
                "john.updated@example.com",
                null
        );

        UserResponseDTO result = userService.updateUser(testUser1.getId(), updateRequest);

        assertNotNull(result);
        assertEquals(testUser1.getId(), result.id());
        assertEquals("JohnUpdated", result.firstName());
        assertEquals("MichaelUpdated", result.secondName());
        assertEquals("DoeUpdated", result.lastName());
        assertEquals(RoleType.MANAGER, result.role());
        assertEquals("john.updated@example.com", result.email());

        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertEquals("JohnUpdated", updatedUser.getFirstName());
        assertEquals("MichaelUpdated", updatedUser.getSecondName());
        assertEquals("DoeUpdated", updatedUser.getLastName());
        assertEquals("john.updated@example.com", updatedUser.getEmail());
        assertEquals(RoleType.MANAGER, updatedUser.getRole());
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenOnlySomeFieldsChanged() {
        UserRequestDTO updateRequest = new UserRequestDTO(
                testUser1.getId(),
                "JohnUpdated",
                testUser1.getSecondName(),
                testUser1.getLastName(),
                testUser1.getRole(),
                testUser1.getEmail(),
                null
        );

        UserResponseDTO result = userService.updateUser(testUser1.getId(), updateRequest);

        assertNotNull(result);
        assertEquals("JohnUpdated", result.firstName());
        assertEquals(testUser1.getSecondName(), result.secondName());
        assertEquals(testUser1.getLastName(), result.lastName());
        assertEquals(testUser1.getEmail(), result.email());
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenSecondNameSetToNull() {
        UserRequestDTO updateRequest = new UserRequestDTO(
                testUser2.getId(),
                testUser2.getFirstName(),
                null,
                testUser2.getLastName(),
                testUser2.getRole(),
                testUser2.getEmail(),
                null
        );

        UserResponseDTO result = userService.updateUser(testUser2.getId(), updateRequest);

        assertNotNull(result);
        assertNull(result.secondName());
        assertEquals(testUser2.getFirstName(), result.firstName());
    }

    @Test
    void updateUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long nonExistentId = 999L;
        UserRequestDTO updateRequest = new UserRequestDTO(
                nonExistentId,
                "Name",
                "Middle",
                "Last",
                RoleType.STUDENT,
                "email@example.com",
                null
        );

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(nonExistentId, updateRequest)
        );

        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentId));
    }

    @Test
    void updateUser_ShouldThrowUserAlreadyExistsException_WhenEmailTakenByOtherUser() {
        UserRequestDTO updateRequest = new UserRequestDTO(
                testUser1.getId(),
                testUser1.getFirstName(),
                testUser1.getSecondName(),
                testUser1.getLastName(),
                testUser1.getRole(),
                "jane.smith@example.com",
                null
        );

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(testUser1.getId(), updateRequest)
        );

        assertTrue(exception.getMessage().contains("Email jane.smith@example.com is already taken"));
    }

    @Test
    void updateUser_ShouldNotThrowException_WhenEmailNotChanged() {
        UserRequestDTO updateRequest = new UserRequestDTO(
                testUser1.getId(),
                "JohnUpdated",
                testUser1.getSecondName(),
                testUser1.getLastName(),
                testUser1.getRole(),
                testUser1.getEmail(),
                null
        );

        assertDoesNotThrow(() -> userService.updateUser(testUser1.getId(), updateRequest));

        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertEquals("JohnUpdated", updatedUser.getFirstName());
        assertEquals(testUser1.getEmail(), updatedUser.getEmail());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        Long userId = testUser1.getId();

        userService.deleteUser(userId);

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void deleteUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long nonExistentId = 999L;

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentId));
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        boolean exists = userService.existsByEmail("john.doe@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        boolean exists = userService.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void getUsersCreatedBetween_ShouldReturnEmptyList_WhenNoUsersInDateRange() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        List<UserResponseDTO> result = userService.getUsersCreatedBetween(start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void countUsersByRole_ShouldReturnCorrectCount() {
        long adminCount = userService.countUsersByRole(RoleType.ADMIN);
        long userCount = userService.countUsersByRole(RoleType.STUDENT);
        long driverCount = userService.countUsersByRole(RoleType.DRIVER);
        long managerCount = userService.countUsersByRole(RoleType.MANAGER);

        assertEquals(1, adminCount);
        assertEquals(2, userCount);
        assertEquals(1, driverCount);
        assertEquals(0, managerCount);
    }


    @Test
    void createUser_ShouldHandleLongNames() {
        UserRequestDTO newUserRequest = new UserRequestDTO(
                null,
                "A".repeat(100),
                "B".repeat(100),
                "C".repeat(100),
                RoleType.STUDENT,
                "long.names@example.com",
                null
        );

        UserResponseDTO result = userService.createUser(newUserRequest);

        assertNotNull(result);
        assertEquals(100, result.firstName().length());
        assertEquals(100, result.secondName().length());
        assertEquals(100, result.lastName().length());
    }

    @Test
    void searchUsersByLastName_ShouldHandleEmptyString() {
        List<UserResponseDTO> result = userService.searchUsersByLastName("");

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    void getUsersCreatedBetween_ShouldHandleSameStartAndEnd() {
        LocalDateTime sameTime = LocalDateTime.now().minusDays(2);

        List<UserResponseDTO> result = userService.getUsersCreatedBetween(sameTime, sameTime);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateUser_ShouldPreserveCreatedAt() {
        LocalDateTime originalCreatedAt = testUser1.getCreatedAt();

        UserRequestDTO updateRequest = new UserRequestDTO(
                testUser1.getId(),
                "UpdatedName",
                testUser1.getSecondName(),
                testUser1.getLastName(),
                testUser1.getRole(),
                testUser1.getEmail(),
                null
        );

        UserResponseDTO result = userService.updateUser(testUser1.getId(), updateRequest);

        assertNotNull(result);
        assertEquals(originalCreatedAt, result.createdAt());

        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertEquals(originalCreatedAt, updatedUser.getCreatedAt());
    }

    @Test
    void getAllUsers_ShouldReturnUsersInConsistentOrder() {
        List<UserResponseDTO> result1 = userService.getAllUsers();
        List<UserResponseDTO> result2 = userService.getAllUsers();

        assertEquals(result1.size(), result2.size());
        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i).id(), result2.get(i).id());
        }
    }
}