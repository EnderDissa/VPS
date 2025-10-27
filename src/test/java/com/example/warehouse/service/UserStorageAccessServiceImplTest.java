package com.example.warehouse.service;

import com.example.warehouse.dto.UserStorageAccessDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    private User testAdmin;
    private Storage testStorage;
    private Storage testStorage2;
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
        UserStorageAccessDTO newAccessDTO = new UserStorageAccessDTO(
                null,
                testUser.getId(),
                testStorage2.getId(),
                AccessLevel.MANAGER,
                testAdmin.getId(),
                null,
                LocalDateTime.now().plusDays(60),
                true
        );

        UserStorageAccessDTO result = userStorageAccessService.create(newAccessDTO);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(AccessLevel.MANAGER, result.accessLevel());
        assertEquals(testUser.getId(), result.userId());
        assertEquals(testStorage2.getId(), result.storageId());
        assertEquals(testAdmin.getId(), result.grantedById());
        assertTrue(result.isActive());

        UserStorageAccess savedAccess = userStorageAccessRepository.findById(result.id()).orElseThrow();
        assertEquals(AccessLevel.MANAGER, savedAccess.getAccessLevel());
        assertNotNull(savedAccess.getGrantedAt());
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        Long nonExistentUserId = 999L;
        UserStorageAccessDTO newAccessDTO = new UserStorageAccessDTO(
                null,
                nonExistentUserId,
                testStorage.getId(),
                AccessLevel.BASIC,
                testAdmin.getId(),
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(UserNotFoundException.class, () -> userStorageAccessService.create(newAccessDTO));
    }

    @Test
    void create_ShouldThrowException_WhenStorageNotFound() {
        Long nonExistentStorageId = 999L;
        UserStorageAccessDTO newAccessDTO = new UserStorageAccessDTO(
                null,
                testUser.getId(),
                nonExistentStorageId,
                AccessLevel.BASIC,
                testAdmin.getId(),
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(StorageNotFoundException.class, () -> userStorageAccessService.create(newAccessDTO));
    }

    @Test
    void create_ShouldThrowException_WhenGrantedByUserNotFound() {
        Long nonExistentGrantedById = 999L;
        UserStorageAccessDTO newAccessDTO = new UserStorageAccessDTO(
                null,
                testUser.getId(),
                testStorage.getId(),
                AccessLevel.BASIC,
                nonExistentGrantedById,
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(UserNotFoundException.class, () -> userStorageAccessService.create(newAccessDTO));
    }

    @Test
    void create_ShouldThrowException_WhenDuplicateUserStorageAccess() {
        UserStorageAccessDTO duplicateAccessDTO = new UserStorageAccessDTO(
                null,
                testUser.getId(),
                testStorage.getId(),
                AccessLevel.MANAGER,
                testAdmin.getId(),
                null,
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(DuplicateUserStorageAccessException.class,
                () -> userStorageAccessService.create(duplicateAccessDTO));
    }

    @Test
    void create_ShouldThrowException_WhenExpirationDateInPast() {
        UserStorageAccessDTO expiredAccessDTO = new UserStorageAccessDTO(
                null,
                testUser.getId(),
                testStorage2.getId(),
                AccessLevel.BASIC,
                testAdmin.getId(),
                null,
                LocalDateTime.now().minusDays(1),
                true
        );

        assertThrows(OperationNotAllowedException.class,
                () -> userStorageAccessService.create(expiredAccessDTO));
    }

    @Test
    void getById_ShouldReturnUserStorageAccess_WhenExists() {
        UserStorageAccessDTO result = userStorageAccessService.getById(testAccess.getId());

        assertNotNull(result);
        assertEquals(testAccess.getId(), result.id());
        assertEquals(testAccess.getAccessLevel(), result.accessLevel());
        assertEquals(testUser.getId(), result.userId());
        assertEquals(testStorage.getId(), result.storageId());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(UserStorageAccessNotFoundException.class,
                () -> userStorageAccessService.getById(nonExistentId));
    }

    @Test
    void update_ShouldUpdateUserStorageAccess_WhenValidData() {
        UserStorageAccessDTO updateDTO = new UserStorageAccessDTO(
                testAccess.getId(),
                testUser.getId(),
                testStorage.getId(),
                AccessLevel.MANAGER,
                testAdmin.getId(),
                testAccess.getGrantedAt(),
                LocalDateTime.now().plusDays(90),
                false
        );

        userStorageAccessService.update(testAccess.getId(), updateDTO);

        UserStorageAccess updatedAccess = userStorageAccessRepository.findById(testAccess.getId()).orElseThrow();
        assertEquals(AccessLevel.MANAGER, updatedAccess.getAccessLevel());
        assertEquals(LocalDateTime.now().plusDays(90).toLocalDate(),
                updatedAccess.getExpiresAt().toLocalDate());
        assertFalse(updatedAccess.getIsActive());
    }

    @Test
    void update_ShouldThrowException_WhenExpirationDateInPast() {
        UserStorageAccessDTO updateDTO = new UserStorageAccessDTO(
                testAccess.getId(),
                testUser.getId(),
                testStorage.getId(),
                AccessLevel.BASIC,
                testAdmin.getId(),
                testAccess.getGrantedAt(),
                LocalDateTime.now().minusDays(1),
                true
        );

        assertThrows(OperationNotAllowedException.class,
                () -> userStorageAccessService.update(testAccess.getId(), updateDTO));
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

        UserStorageAccessDTO updateDTO = new UserStorageAccessDTO(
                secondAccess.getId(),
                testUser.getId(),
                testStorage.getId(),
                AccessLevel.BASIC,
                testAdmin.getId(),
                secondAccess.getGrantedAt(),
                LocalDateTime.now().plusDays(30),
                true
        );

        assertThrows(DuplicateUserStorageAccessException.class,
                () -> userStorageAccessService.update(secondAccess.getId(), updateDTO));
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
        Page<UserStorageAccessDTO> result = userStorageAccessService.findPage(
                0, 10, testUser.getId(), null, null, null
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(testUser.getId(), result.getContent().get(0).userId());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithStorageFilter() {
        Page<UserStorageAccessDTO> result = userStorageAccessService.findPage(
                0, 10, null, testStorage.getId(), null, null
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(testStorage.getId(), result.getContent().get(0).storageId());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithAccessLevelFilter() {
        Page<UserStorageAccessDTO> result = userStorageAccessService.findPage(
                0, 10, null, null, AccessLevel.BASIC, null
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertEquals(AccessLevel.BASIC, result.getContent().get(0).accessLevel());
    }

    @Test
    void findPage_ShouldReturnFilteredResults_WithActiveFilter() {
        Page<UserStorageAccessDTO> result = userStorageAccessService.findPage(
                0, 10, null, null, null, true
        );

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void findPage_ShouldReturnEmpty_WhenNoMatches() {
        Page<UserStorageAccessDTO> result = userStorageAccessService.findPage(
                0, 10, 999L, null, null, null
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByUserAndStorage_ShouldReturnAccess_WhenExists() {
        UserStorageAccessDTO result = userStorageAccessService.findByUserAndStorage(
                testUser.getId(), testStorage.getId()
        );

        assertNotNull(result);
        assertEquals(testUser.getId(), result.userId());
        assertEquals(testStorage.getId(), result.storageId());
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
        UserStorageAccessDTO result = userStorageAccessService.deactivate(testAccess.getId());

        assertFalse(result.isActive());

        UserStorageAccess deactivatedAccess = userStorageAccessRepository.findById(testAccess.getId()).orElseThrow();
        assertFalse(deactivatedAccess.getIsActive());
    }

    @Test
    void activate_ShouldActivateAccess() {
        testAccess.setIsActive(false);
        userStorageAccessRepository.save(testAccess);

        UserStorageAccessDTO result = userStorageAccessService.activate(testAccess.getId());

        assertTrue(result.isActive());

        UserStorageAccess activatedAccess = userStorageAccessRepository.findById(testAccess.getId()).orElseThrow();
        assertTrue(activatedAccess.getIsActive());
    }

    @Test
    void findByUser_ShouldReturnUserAccesses() {
        List<UserStorageAccessDTO> result = userStorageAccessService.findByUser(testUser.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testUser.getId(), result.get(0).userId());
    }

    @Test
    void findByStorage_ShouldReturnStorageAccesses() {
        List<UserStorageAccessDTO> result = userStorageAccessService.findByStorage(testStorage.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testStorage.getId(), result.get(0).storageId());
    }

    @Test
    void findExpiredAccesses_ShouldReturnExpiredAccesses() {
        testAccess.setExpiresAt(LocalDateTime.now().minusDays(1));
        userStorageAccessRepository.save(testAccess);

        List<UserStorageAccessDTO> result = userStorageAccessService.findExpiredAccesses();

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
        Page<UserStorageAccessDTO> result1 = userStorageAccessService.findPage(0, 10, testUser.getId(), testStorage.getId(), null, null);
        assertNotNull(result1);

        Page<UserStorageAccessDTO> result2 = userStorageAccessService.findPage(0, 10, testUser.getId(), null, AccessLevel.BASIC, null);
        assertNotNull(result2);

        Page<UserStorageAccessDTO> result3 = userStorageAccessService.findPage(0, 10, null, testStorage.getId(), AccessLevel.BASIC, true);
        assertNotNull(result3);

        Page<UserStorageAccessDTO> result4 = userStorageAccessService.findPage(0, 10, null, null, null, null);
        assertNotNull(result4);
    }
}