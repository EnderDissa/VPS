//package com.example.warehouse.service;
//
//import com.example.warehouse.entity.User;
//import com.example.warehouse.enumeration.RoleType;
//import com.example.warehouse.exception.UserAlreadyExistsException;
//import com.example.warehouse.exception.UserNotFoundException;
//import com.example.warehouse.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.jdbc.Sql;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import java.time.LocalDateTime;
//import java.util.List;
//import static org.junit.jupiter.api.Assertions.*;
//
//@Testcontainers
//@SpringBootTest
//@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//class UserServiceImplIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
//            .withDatabaseName("testdb")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired
//    private UserServiceImpl userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User testUser1;
//    private User testUser2;
//    private User testUser3;
//    private User testUser4;
//
//    @BeforeEach
//    void setUp() {
//        userRepository.deleteAll();
//
//        testUser1 = User.builder()
//                .firstName("John")
//                .secondName("Michael")
//                .lastName("Doe")
//                .email("john.doe@example.com")
//                .role(RoleType.ADMIN)
//                .createdAt(LocalDateTime.now().minusDays(5))
//                .build();
//
//        testUser2 = User.builder()
//                .firstName("Jane")
//                .secondName("Marie")
//                .lastName("Smith")
//                .email("jane.smith@example.com")
//                .role(RoleType.STUDENT)
//                .createdAt(LocalDateTime.now().minusDays(3))
//                .build();
//
//        testUser3 = User.builder()
//                .firstName("Bob")
//                .secondName(null)
//                .lastName("Johnson")
//                .email("bob.johnson@example.com")
//                .role(RoleType.DRIVER)
//                .createdAt(LocalDateTime.now().minusDays(1))
//                .build();
//
//        testUser4 = User.builder()
//                .firstName("Alice")
//                .secondName("Ann")
//                .lastName("Williams")
//                .email("alice.williams@example.com")
//                .role(RoleType.STUDENT)
//                .createdAt(LocalDateTime.now().minusHours(12))
//                .build();
//
//        testUser1 = userRepository.save(testUser1);
//        testUser2 = userRepository.save(testUser2);
//        testUser3 = userRepository.save(testUser3);
//        testUser4 = userRepository.save(testUser4);
//    }
//
//    @Test
//    void createUser_ShouldCreateUser_WhenValidData() {
//        User newUser = new User(
//                null,
//                "New",
//                "Middle",
//                "User",
//                RoleType.STUDENT,
//                "new.user@example.com",
//                null
//        );
//
//        User result = userService.createUser(newUser);
//
//        assertNotNull(result);
//        assertNotNull(result.getId());
//        assertEquals("New", result.getFirstName());
//        assertEquals("Middle", result.getSecondName());
//        assertEquals("User", result.getLastName());
//        assertEquals(RoleType.STUDENT, result.getRole());
//        assertEquals("new.user@example.com", result.getEmail());
//        assertNotNull(result.getCreatedAt());
//
//        User savedUser = userRepository.findById(result.getId()).orElseThrow();
//        assertEquals("New", savedUser.getFirstName());
//        assertEquals("Middle", savedUser.getSecondName());
//        assertEquals("User", savedUser.getLastName());
//        assertEquals("new.user@example.com", savedUser.getEmail());
//    }
//
//    @Test
//    void createUser_ShouldCreateUser_WhenSecondNameIsNull() {
//        User newUser = new User(
//                null,
//                "NoMiddle",
//                null,
//                "Name",
//                RoleType.DRIVER,
//                "nomiddle.name@example.com",
//                null
//        );
//
//        User result = userService.createUser(newUser);
//
//        assertNotNull(result);
//        assertNull(result.getSecondName());
//        assertEquals("NoMiddle", result.getFirstName());
//        assertEquals("Name", result.getLastName());
//    }
//
//    @Test
//    void createUser_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
//        User duplicateUser = new User(
//                null,
//                "Different",
//                "Name",
//                "User",
//                RoleType.STUDENT,
//                "john.doe@example.com",
//                null
//        );
//
//        UserAlreadyExistsException exception = assertThrows(
//                UserAlreadyExistsException.class,
//                () -> userService.createUser(duplicateUser)
//        );
//
//        assertTrue(exception.getMessage().contains("User with email john.doe@example.com already exists"));
//    }
//
//    @Test
//    void getUserById_ShouldReturnUser_WhenUserExists() {
//        User result = userService.getUserById(testUser1.getId());
//
//        assertNotNull(result);
//        assertEquals(testUser1.getId(), result.getId());
//        assertEquals(testUser1.getFirstName(), result.getFirstName());
//        assertEquals(testUser1.getSecondName(), result.getSecondName());
//        assertEquals(testUser1.getLastName(), result.getLastName());
//        assertEquals(testUser1.getEmail(), result.getEmail());
//        assertEquals(testUser1.getRole(), result.getRole());
//        assertEquals(testUser1.getCreatedAt(), result.getCreatedAt());
//    }
//
//    @Test
//    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
//        Long nonExistentId = 999L;
//
//        UserNotFoundException exception = assertThrows(
//                UserNotFoundException.class,
//                () -> userService.getUserById(nonExistentId)
//        );
//
//        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
//        User result = userService.getUserByEmail("jane.smith@example.com");
//
//        assertNotNull(result);
//        assertEquals(testUser2.getId(), result.getId());
//        assertEquals(testUser2.getFirstName(), result.getFirstName());
//        assertEquals(testUser2.getLastName(), result.getLastName());
//        assertEquals("jane.smith@example.com", result.getEmail());
//    }
//
//    @Test
//    void getUserByEmail_ShouldThrowUserNotFoundException_WhenUserNotFound() {
//        String nonExistentEmail = "nonexistent@example.com";
//
//        UserNotFoundException exception = assertThrows(
//                UserNotFoundException.class,
//                () -> userService.getUserByEmail(nonExistentEmail)
//        );
//
//        assertTrue(exception.getMessage().contains("User not found with email: " + nonExistentEmail));
//    }
//
//    @Test
//    void getAllUsers_ShouldReturnAllUsers() {
//        List<User> result = userService.getAllUsers();
//
//        assertNotNull(result);
//        assertEquals(4, result.size());
//
//        assertTrue(result.stream().anyMatch(user -> user.getEmail().equals("john.doe@example.com")));
//        assertTrue(result.stream().anyMatch(user -> user.getEmail().equals("jane.smith@example.com")));
//        assertTrue(result.stream().anyMatch(user -> user.getEmail().equals("bob.johnson@example.com")));
//        assertTrue(result.stream().anyMatch(user -> user.getEmail().equals("alice.williams@example.com")));
//    }
//
//    @Test
//    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
//        userRepository.deleteAll();
//
//        List<User> result = userService.getAllUsers();
//
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void getUsersByRole_ShouldReturnUsers_WhenRoleExists() {
//        List<User> result = userService.getUsersByRole(RoleType.STUDENT);
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.stream().allMatch(user -> user.getRole() == RoleType.STUDENT));
//
//        assertTrue(result.stream().anyMatch(user -> user.getEmail().equals("jane.smith@example.com")));
//        assertTrue(result.stream().anyMatch(user -> user.getEmail().equals("alice.williams@example.com")));
//    }
//
//    @Test
//    void getUsersByRole_ShouldReturnEmptyList_WhenNoUsersWithRole() {
//        List<User> result = userService.getUsersByRole(RoleType.MANAGER);
//
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void searchUsersByLastName_ShouldReturnUsers_WhenLastNameMatches() {
//        List<User> result = userService.searchUsersByLastName("smith");
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("Jane", result.get(0).getFirstName());
//        assertEquals("Smith", result.get(0).getLastName());
//    }
//
//    @Test
//    void searchUsersByLastName_ShouldReturnUsers_WhenCaseInsensitive() {
//        List<User> result = userService.searchUsersByLastName("SMITH");
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("Smith", result.get(0).getLastName());
//    }
//
//    @Test
//    void searchUsersByLastName_ShouldReturnUsers_WhenPartialMatch() {
//        List<User> result = userService.searchUsersByLastName("son");
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("Johnson", result.get(0).getLastName());
//    }
//
//    @Test
//    void searchUsersByLastName_ShouldReturnMultipleUsers_WhenMultipleMatches() {
//        User anotherSmith = User.builder()
//                .firstName("Tom")
//                .lastName("Smith")
//                .email("tom.smith@example.com")
//                .role(RoleType.STUDENT)
//                .createdAt(LocalDateTime.now())
//                .build();
//        userRepository.save(anotherSmith);
//
//        List<User> result = userService.searchUsersByLastName("Smith");
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.stream().allMatch(user -> user.getLastName().equals("Smith")));
//    }
//
//    @Test
//    void searchUsersByLastName_ShouldReturnEmptyList_WhenNoMatches() {
//        List<User> result = userService.searchUsersByLastName("Nonexistent");
//
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void updateUser_ShouldUpdateUser_WhenValidData() {
//        User update = new User(
//                testUser1.getId(),
//                "JohnUpdated",
//                "MichaelUpdated",
//                "DoeUpdated",
//                RoleType.MANAGER,
//                "john.updated@example.com",
//                null
//        );
//
//        User result = userService.updateUser(testUser1.getId(), update);
//
//        assertNotNull(result);
//        assertEquals(testUser1.getId(), result.getId());
//        assertEquals("JohnUpdated", result.getFirstName());
//        assertEquals("MichaelUpdated", result.getSecondName());
//        assertEquals("DoeUpdated", result.getLastName());
//        assertEquals(RoleType.MANAGER, result.getRole());
//        assertEquals("john.updated@example.com", result.getEmail());
//
//        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
//        assertEquals("JohnUpdated", updatedUser.getFirstName());
//        assertEquals("MichaelUpdated", updatedUser.getSecondName());
//        assertEquals("DoeUpdated", updatedUser.getLastName());
//        assertEquals("john.updated@example.com", updatedUser.getEmail());
//        assertEquals(RoleType.MANAGER, updatedUser.getRole());
//    }
//
//    @Test
//    void updateUser_ShouldUpdateUser_WhenOnlySomeFieldsChanged() {
//        User update = new User(
//                testUser1.getId(),
//                "JohnUpdated",
//                testUser1.getSecondName(),
//                testUser1.getLastName(),
//                testUser1.getRole(),
//                testUser1.getEmail(),
//                null
//        );
//
//        User result = userService.updateUser(testUser1.getId(), update);
//
//        assertNotNull(result);
//        assertEquals("JohnUpdated", result.getFirstName());
//        assertEquals(testUser1.getSecondName(), result.getSecondName());
//        assertEquals(testUser1.getLastName(), result.getLastName());
//        assertEquals(testUser1.getEmail(), result.getEmail());
//    }
//
//    @Test
//    void updateUser_ShouldUpdateUser_WhenSecondNameSetToNull() {
//        User update = new User(
//                testUser2.getId(),
//                testUser2.getFirstName(),
//                null,
//                testUser2.getLastName(),
//                testUser2.getRole(),
//                testUser2.getEmail(),
//                null
//        );
//
//        User result = userService.updateUser(testUser2.getId(), update);
//
//        assertNotNull(result);
//        assertNull(result.getSecondName());
//        assertEquals(testUser2.getFirstName(), result.getFirstName());
//    }
//
//    @Test
//    void updateUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
//        Long nonExistentId = 999L;
//        User update = new User(
//                nonExistentId,
//                "Name",
//                "Middle",
//                "Last",
//                RoleType.STUDENT,
//                "email@example.com",
//                null
//        );
//
//        UserNotFoundException exception = assertThrows(
//                UserNotFoundException.class,
//                () -> userService.updateUser(nonExistentId, update)
//        );
//
//        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void updateUser_ShouldThrowUserAlreadyExistsException_WhenEmailTakenByOtherUser() {
//        User update = new User(
//                testUser1.getId(),
//                testUser1.getFirstName(),
//                testUser1.getSecondName(),
//                testUser1.getLastName(),
//                testUser1.getRole(),
//                "jane.smith@example.com",
//                null
//        );
//
//        UserAlreadyExistsException exception = assertThrows(
//                UserAlreadyExistsException.class,
//                () -> userService.updateUser(testUser1.getId(), update)
//        );
//
//        assertTrue(exception.getMessage().contains("Email jane.smith@example.com is already taken"));
//    }
//
//    @Test
//    void updateUser_ShouldNotThrowException_WhenEmailNotChanged() {
//        User update = new User(
//                testUser1.getId(),
//                "JohnUpdated",
//                testUser1.getSecondName(),
//                testUser1.getLastName(),
//                testUser1.getRole(),
//                testUser1.getEmail(),
//                null
//        );
//
//        assertDoesNotThrow(() -> userService.updateUser(testUser1.getId(), update));
//
//        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
//        assertEquals("JohnUpdated", updatedUser.getFirstName());
//        assertEquals(testUser1.getEmail(), updatedUser.getEmail());
//    }
//
//    @Test
//    void deleteUser_ShouldDeleteUser_WhenUserExists() {
//        Long userId = testUser1.getId();
//
//        userService.deleteUser(userId);
//
//        assertFalse(userRepository.existsById(userId));
//    }
//
//    @Test
//    void deleteUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
//        Long nonExistentId = 999L;
//
//        UserNotFoundException exception = assertThrows(
//                UserNotFoundException.class,
//                () -> userService.deleteUser(nonExistentId)
//        );
//
//        assertTrue(exception.getMessage().contains("User not found with ID: " + nonExistentId));
//    }
//
//    @Test
//    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
//        boolean exists = userService.existsByEmail("john.doe@example.com");
//
//        assertTrue(exists);
//    }
//
//    @Test
//    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
//        boolean exists = userService.existsByEmail("nonexistent@example.com");
//
//        assertFalse(exists);
//    }
//
//    @Test
//    void getUsersCreatedBetween_ShouldReturnEmptyList_WhenNoUsersInDateRange() {
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = LocalDateTime.now().plusDays(2);
//
//        List<User> result = userService.getUsersCreatedBetween(start, end);
//
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void countUsersByRole_ShouldReturnCorrectCount() {
//        long adminCount = userService.countUsersByRole(RoleType.ADMIN);
//        long userCount = userService.countUsersByRole(RoleType.STUDENT);
//        long driverCount = userService.countUsersByRole(RoleType.DRIVER);
//        long managerCount = userService.countUsersByRole(RoleType.MANAGER);
//
//        assertEquals(1, adminCount);
//        assertEquals(2, userCount);
//        assertEquals(1, driverCount);
//        assertEquals(0, managerCount);
//    }
//
//
//    @Test
//    void createUser_ShouldHandleLongNames() {
//        User newUser = new User(
//                null,
//                "A".repeat(100),
//                "B".repeat(100),
//                "C".repeat(100),
//                RoleType.STUDENT,
//                "long.names@example.com",
//                null
//        );
//
//        User result = userService.createUser(newUser);
//
//        assertNotNull(result);
//        assertEquals(100, result.getFirstName().length());
//        assertEquals(100, result.getSecondName().length());
//        assertEquals(100, result.getLastName().length());
//    }
//
//    @Test
//    void searchUsersByLastName_ShouldHandleEmptyString() {
//        List<User> result = userService.searchUsersByLastName("");
//
//        assertNotNull(result);
//        assertEquals(4, result.size());
//    }
//
//    @Test
//    void getUsersCreatedBetween_ShouldHandleSameStartAndEnd() {
//        LocalDateTime sameTime = LocalDateTime.now().minusDays(2);
//
//        List<User> result = userService.getUsersCreatedBetween(sameTime, sameTime);
//
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void updateUser_ShouldPreserveCreatedAt() {
//        LocalDateTime originalCreatedAt = testUser1.getCreatedAt();
//
//        User update = new User(
//                testUser1.getId(),
//                "UpdatedName",
//                testUser1.getSecondName(),
//                testUser1.getLastName(),
//                testUser1.getRole(),
//                testUser1.getEmail(),
//                LocalDateTime.now()
//        );
//
//        User result = userService.updateUser(testUser1.getId(), update);
//
//        assertNotNull(result);
//        assertNotEquals(originalCreatedAt, result.getCreatedAt());
//
//        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
//        assertNotEquals(originalCreatedAt, updatedUser.getCreatedAt());
//    }
//
//    @Test
//    void getAllUsers_ShouldReturnUsersInConsistentOrder() {
//        List<User> result1 = userService.getAllUsers();
//        List<User> result2 = userService.getAllUsers();
//
//        assertEquals(result1.size(), result2.size());
//        for (int i = 0; i < result1.size(); i++) {
//            assertEquals(result1.get(i).getId(), result2.get(i).getId());
//        }
//    }
//}