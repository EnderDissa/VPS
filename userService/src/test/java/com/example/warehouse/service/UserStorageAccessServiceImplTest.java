package com.example.warehouse.service;

import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.enumeration.AccessLevel;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.*;
import com.example.warehouse.repository.UserStorageAccessRepository;
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
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserStorageAccessServiceImplIntegrationTest {

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
    private UserStorageAccessServiceImpl userStorageAccessService;

    @Autowired
    private UserStorageAccessRepository userStorageAccessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageRepository storageRepository;

    private User testUser;
    private User nonExistentUser;
    private User testAdmin;
    private Storage testStorage;
    private Storage testStorage2;
    private Storage nonExistentStorage;
    private UserStorageAccess testAccess;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("John")
                .secondName("Michael")
                .lastName("Doe")
                .role(RoleType.STUDENT)
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        nonExistentUser = User.builder()
                .id(999L)
                .firstName("John")
                .secondName("Michael")
                .lastName("Doe")
                .role(RoleType.STUDENT)
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        testAdmin = User.builder()
                .firstName("Admin")
                .secondName("User")
                .lastName("Smith")
                .role(RoleType.ADMIN)
                .email("admin@example.com")
                .createdAt(LocalDateTime.now())
                .build();
        testAdmin = userRepository.save(testAdmin);

        testStorage = Storage.builder()
                .name("Main Storage")
                .address("123 Main St, City")
                .capacity(1000)
                .createdAt(LocalDateTime.now())
                .build();
        testStorage = storageRepository.save(testStorage);

        testStorage2 = Storage.builder()
                .name("Secondary Storage")
                .address("456 Oak St, Town")
                .capacity(500)
                .createdAt(LocalDateTime.now())
                .build();
        testStorage2 = storageRepository.save(testStorage2);

        nonExistentStorage = Storage.builder()
                .id(999L)
                .name("Secondary Storage")
                .address("456 Oak St, Town")
                .capacity(500)
                .createdAt(LocalDateTime.now())
                .build();

        testAccess = UserStorageAccess.builder()
                .user(testUser)
                .storage(testStorage)
                .grantedBy(testAdmin)
                .accessLevel(AccessLevel.BASIC)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();
        testAccess = userStorageAccessRepository.save(testAccess);
    }

    @Test
    void create_ShouldCreateUserStorageAccess_WhenValidData() {
        UserStorageAccess newAccess = new UserStorageAccess(
                null,
                testUser,
                testStorage2,
                AccessLevel.MANAGER,
                testAdmin,
                null,
                LocalDateTime.now().plusDays(60),
                true
        );

        UserStorageAccess result = userStorageAccessService.create(newAccess);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(AccessLevel.MANAGER, result.getAccessLevel());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(testStorage2.getId(), result.getStorage().getId());
        assertEquals(testAdmin.getId(), result.getGrantedBy().getId());
        assertTrue(result.getIsActive());

        UserStorageAccess savedAccess = userStorageAccessRepository.findById(result.getId()).orElseThrow();
        assertEquals(AccessLevel.MANAGER, savedAccess.getAccessLevel());
        assertNotNull(savedAccess.getGrantedAt());
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        Long nonExistentUserId = 999L;
        UserStorageAccess newAccess = new UserStorageAccess(
                null,
                nonExistentUser,
                testStorage,
                AccessLevel.BASIC,
                testAdmin,
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(UserNotFoundException.class, () -> userStorageAccessService.create(newAccess));
    }

    @Test
    void create_ShouldThrowException_WhenStorageNotFound() {
        Long nonExistentStorageId = 999L;
        UserStorageAccess newAccess = new UserStorageAccess(
                null,
                testUser,
                nonExistentStorage,
                AccessLevel.BASIC,
                testAdmin,
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(StorageNotFoundException.class, () -> userStorageAccessService.create(newAccess));
    }

    @Test
    void create_ShouldThrowException_WhenGrantedByUserNotFound() {
        Long nonExistentGrantedById = 999L;
        UserStorageAccess newAccess = new UserStorageAccess(
                null,
                testUser,
                testStorage,
                AccessLevel.BASIC,
                nonExistentUser,
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(UserNotFoundException.class, () -> userStorageAccessService.create(newAccess));
    }

    @Test
    void create_ShouldThrowException_WhenDuplicateUserStorageAccess() {
        UserStorageAccess duplicateAccess = new UserStorageAccess(
                null,
                testUser,
                testStorage,
                AccessLevel.MANAGER,
                testAdmin,
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(DuplicateUserStorageAccessException.class,
                () -> userStorageAccessService.create(duplicateAccess));
    }

    @Test
    void create_ShouldThrowException_WhenExpirationDateInPast() {
        UserStorageAccess expiredAccess = new UserStorageAccess(
                null,
                testUser,
                testStorage2,
                AccessLevel.BASIC,
                testAdmin,
                null,
                LocalDateTime.now().minusDays(1),
                true
        );

        assertThrows(OperationNotAllowedException.class,
                () -> userStorageAccessService.create(expiredAccess));
    }

    @Test
    void getById_ShouldReturnUserStorageAccess_WhenExists() {
        UserStorageAccess result = userStorageAccessService.getById(testAccess.getId());

        assertNotNull(result);
        assertEquals(testAccess.getId(), result.getId());
        assertEquals(testAccess.getAccessLevel(), result.getAccessLevel());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(testStorage.getId(), result.getStorage().getId());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(UserStorageAccessNotFoundException.class,
                () -> userStorageAccessService.getById(nonExistentId));
    }

    @Test
    void update_ShouldUpdateUserStorageAccess_WhenValidData() {
        UserStorageAccess update = new UserStorageAccess(
                testAccess.getId(),
                testUser,
                testStorage,
                AccessLevel.MANAGER,
                testAdmin,
                testAccess.getGrantedAt(),
                LocalDateTime.now().plusDays(90),
                false
        );

        userStorageAccessService.update(testAccess.getId(), update);

        UserStorageAccess updatedAccess = userStorageAccessRepository.findById(testAccess.getId()).orElseThrow();
        assertEquals(AccessLevel.MANAGER, updatedAccess.getAccessLevel());
        assertEquals(LocalDateTime.now().plusDays(90).toLocalDate(),
                updatedAccess.getExpiresAt().toLocalDate());
        assertFalse(updatedAccess.getIsActive());
    }

    @Test
    void update_ShouldThrowException_WhenExpirationDateInPast() {
        UserStorageAccess update = new UserStorageAccess(
                testAccess.getId(),
                testUser,
                testStorage,
                AccessLevel.BASIC,
                testAdmin,
                testAccess.getGrantedAt(),
                LocalDateTime.now().minusDays(1),
                true
        );

        assertThrows(OperationNotAllowedException.class,
                () -> userStorageAccessService.update(testAccess.getId(), update));
    }

    @Test
    void update_ShouldThrowException_WhenDuplicateAfterUserChange() {
        final UserStorageAccess secondAccess = userStorageAccessRepository.save(
                UserStorageAccess.builder()
                        .user(testAdmin)
                        .storage(testStorage2)
                        .grantedBy(testAdmin)
                        .accessLevel(AccessLevel.BASIC)
                        .grantedAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .isActive(true)
                        .build()
        );

        UserStorageAccess update = new UserStorageAccess(
                secondAccess.getId(),
                testUser,
                testStorage,
                AccessLevel.BASIC,
                testAdmin,
                secondAccess.getGrantedAt(),
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(DuplicateUserStorageAccessException.class,
                () -> userStorageAccessService.update(secondAccess.getId(), update));
    }

    @Test
    void delete_ShouldDeleteUserStorageAccess_WhenExists() {
        Long accessId = testAccess.getId();

        userStorageAccessService.delete(accessId);

        assertFalse(userStorageAccessRepository.existsById(accessId));
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(UserStorageAccessNotFoundException.class,
                () -> userStorageAccessService.delete(nonExistentId));
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithUserFilter() {
        Page<UserStorageAccess> result = userStorageAccessService.findPage(
                0, 10, testUser.getId(), null, null, null
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(testUser.getId(), result.getContent().get(0).getUser().getId());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithStorageFilter() {
        Page<UserStorageAccess> result = userStorageAccessService.findPage(
                0, 10, null, testStorage.getId(), null, null
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(testStorage.getId(), result.getContent().get(0).getStorage().getId());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithAccessLevelFilter() {
        Page<UserStorageAccess> result = userStorageAccessService.findPage(
                0, 10, null, null, AccessLevel.BASIC, null
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(AccessLevel.BASIC, result.getContent().get(0).getAccessLevel());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithActiveFilter() {
        Page<UserStorageAccess> result = userStorageAccessService.findPage(
                0, 10, null, null, null, true
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().get(0).getIsActive());
    }

    @Test
    void findPage_ShouldReturnEmpty_WhenNoMatches() {
        Page<UserStorageAccess> result = userStorageAccessService.findPage(
                0, 10, 999L, null, null, null
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByUserAndStorage_ShouldReturnAccess_WhenExists() {
        UserStorageAccess result = userStorageAccessService.findByUserAndStorage(
                testUser.getId(), testStorage.getId()
        );

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(testStorage.getId(), result.getStorage().getId());
    }

    @Test
    void findByUserAndStorage_ShouldThrowException_WhenNotFound() {
        assertThrows(UserStorageAccessNotFoundException.class,
                () -> userStorageAccessService.findByUserAndStorage(999L, 999L));
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenValidAccessExists() {
        boolean result = userStorageAccessService.hasAccess(
                testUser.getId(), testStorage.getId(), AccessLevel.BASIC
        );

        assertTrue(result);
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenAccessNotExists() {
        boolean result = userStorageAccessService.hasAccess(
                999L, 999L, AccessLevel.BASIC
        );

        assertFalse(result);
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenAccessInactive() {
        testAccess.setIsActive(false);
        userStorageAccessRepository.save(testAccess);

        boolean result = userStorageAccessService.hasAccess(
                testUser.getId(), testStorage.getId(), AccessLevel.BASIC
        );

        assertFalse(result);
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenAccessExpired() {
        testAccess.setExpiresAt(LocalDateTime.now().minusDays(1));
        userStorageAccessRepository.save(testAccess);

        boolean result = userStorageAccessService.hasAccess(
                testUser.getId(), testStorage.getId(), AccessLevel.BASIC
        );

        assertFalse(result);
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenHigherAccessLevel() {
        testAccess.setAccessLevel(AccessLevel.MANAGER);
        userStorageAccessRepository.save(testAccess);

        boolean result = userStorageAccessService.hasAccess(
                testUser.getId(), testStorage.getId(), AccessLevel.BASIC
        );

        assertTrue(result);
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenLowerAccessLevel() {
        testAccess.setAccessLevel(AccessLevel.BASIC);
        userStorageAccessRepository.save(testAccess);

        boolean result = userStorageAccessService.hasAccess(
                testUser.getId(), testStorage.getId(), AccessLevel.MANAGER
        );

        assertFalse(result);
    }

    @Test
    void deactivate_ShouldDeactivateAccess() {
        UserStorageAccess result = userStorageAccessService.deactivate(testAccess.getId());

        assertFalse(result.getIsActive());

        UserStorageAccess deactivatedAccess = userStorageAccessRepository.findById(testAccess.getId()).orElseThrow();
        assertFalse(deactivatedAccess.getIsActive());
    }

    @Test
    void activate_ShouldActivateAccess() {
        testAccess.setIsActive(false);
        userStorageAccessRepository.save(testAccess);

        UserStorageAccess result = userStorageAccessService.activate(testAccess.getId());

        assertTrue(result.getIsActive());

        UserStorageAccess activatedAccess = userStorageAccessRepository.findById(testAccess.getId()).orElseThrow();
        assertTrue(activatedAccess.getIsActive());
    }

    @Test
    void findByUser_ShouldReturnUserAccesses() {
        List<UserStorageAccess> result = userStorageAccessService.findByUser(testUser.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testUser.getId(), result.get(0).getUser().getId());
    }

    @Test
    void findByStorage_ShouldReturnStorageAccesses() {
        List<UserStorageAccess> result = userStorageAccessService.findByStorage(testStorage.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testStorage.getId(), result.get(0).getStorage().getId());
    }

    @Test
    void findExpiredAccesses_ShouldReturnExpiredAccesses() {
        testAccess.setExpiresAt(LocalDateTime.now().minusDays(1));
        userStorageAccessRepository.save(testAccess);

        List<UserStorageAccess> result = userStorageAccessService.findExpiredAccesses();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void deactivateExpiredAccesses_ShouldDeactivateAllExpired() {
        testAccess.setExpiresAt(LocalDateTime.now().minusDays(1));
        testAccess.setIsActive(true);
        userStorageAccessRepository.save(testAccess);

        userStorageAccessService.deactivateExpiredAccesses();

        UserStorageAccess updatedAccess = userStorageAccessRepository.findById(testAccess.getId()).orElseThrow();
        assertFalse(updatedAccess.getIsActive());
    }

    @Test
    void countActiveAccessesByUser_ShouldReturnCorrectCount() {
        long count = userStorageAccessService.countActiveAccessesByUser(testUser.getId());

        assertEquals(1, count);
    }

    @Test
    void countActiveAccessesByStorage_ShouldReturnCorrectCount() {
        long count = userStorageAccessService.countActiveAccessesByStorage(testStorage.getId());

        assertEquals(1, count);
    }

    @Test
    void findPage_ShouldReturnAllCombinations_WithMultipleFilters() {
        Page<UserStorageAccess> result1 = userStorageAccessService.findPage(0, 10, testUser.getId(), testStorage.getId(), null, null);
        assertNotNull(result1);

        Page<UserStorageAccess> result2 = userStorageAccessService.findPage(0, 10, testUser.getId(), null, AccessLevel.BASIC, null);
        assertNotNull(result2);

        Page<UserStorageAccess> result3 = userStorageAccessService.findPage(0, 10, null, testStorage.getId(), AccessLevel.BASIC, true);
        assertNotNull(result3);

        Page<UserStorageAccess> result4 = userStorageAccessService.findPage(0, 10, null, null, null, null);
        assertNotNull(result4);
    }
}