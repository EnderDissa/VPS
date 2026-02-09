package com.example.warehouse.service;

import com.example.warehouse.infrastructure.persistence.entity.UserStorageAccess;
import com.example.warehouse.domain.enumeration.AccessLevel;
import com.example.warehouse.exception.DuplicateUserStorageAccessException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.exception.UserStorageAccessNotFoundException;
import com.example.warehouse.application.input.UserStorageAccessServiceImpl;
import com.example.warehouse.application.output.UserStorageAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStorageAccessServiceImplTest {

    @Mock
    private UserStorageAccessRepository userStorageAccessRepository;

    @InjectMocks
    private UserStorageAccessServiceImpl userStorageAccessService;

    private UserStorageAccess testAccess;
    private UserStorageAccess testAccess2;
    private UserStorageAccess expiredAccess;
    private UserStorageAccess inactiveAccess;

    @BeforeEach
    void setUp() {
        testAccess = UserStorageAccess.builder()
                .id(1L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.MANAGER)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        testAccess2 = UserStorageAccess.builder()
                .id(2L)
                .userId(101L)
                .storageId(201L)
                .accessLevel(AccessLevel.BASIC)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(2))
                .expiresAt(LocalDateTime.now().plusDays(15))
                .isActive(true)
                .build();

        expiredAccess = UserStorageAccess.builder()
                .id(3L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.BASIC)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(10))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();

        inactiveAccess = UserStorageAccess.builder()
                .id(4L)
                .userId(102L)
                .storageId(202L)
                .accessLevel(AccessLevel.ADMIN)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(5))
                .expiresAt(LocalDateTime.now().plusDays(25))
                .isActive(false)
                .build();
    }


    @Test
    void create_ShouldThrowOperationNotAllowedException_WhenExpirationDateInPast() {
        UserStorageAccess newAccess = UserStorageAccess.builder()
                .userId(103L)
                .storageId(203L)
                .accessLevel(AccessLevel.BASIC)
                .grantedById(300L)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();

        StepVerifier.create(userStorageAccessService.create(newAccess))
                .expectErrorMatches(throwable ->
                        throwable instanceof OperationNotAllowedException &&
                                throwable.getMessage().contains("Expiration date must be in the future")
                )
                .verify();

        verify(userStorageAccessRepository, never()).existsByUserIdAndStorageIdAndIdNot(anyLong(), anyLong(), anyLong());
        verify(userStorageAccessRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnUserStorageAccess_WhenExists() {
        when(userStorageAccessRepository.findById(1L))
                .thenReturn(Mono.just(testAccess));

        StepVerifier.create(userStorageAccessService.getById(1L))
                .assertNext(access -> {
                    assertEquals(testAccess.getId(), access.getId());
                    assertEquals(testAccess.getUserId(), access.getUserId());
                    assertEquals(testAccess.getStorageId(), access.getStorageId());
                    assertEquals(testAccess.getAccessLevel(), access.getAccessLevel());
                })
                .verifyComplete();

        verify(userStorageAccessRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowUserStorageAccessNotFoundException_WhenNotFound() {
        Long nonExistentId = 999L;
        when(userStorageAccessRepository.findById(nonExistentId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userStorageAccessService.getById(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserStorageAccessNotFoundException &&
                                throwable.getMessage().contains("User storage access not found with ID: " + nonExistentId)
                )
                .verify();

        verify(userStorageAccessRepository).findById(nonExistentId);
    }

    @Test
    void update_ShouldThrowOperationNotAllowedException_WhenExpirationDateInPast() {
        UserStorageAccess update = UserStorageAccess.builder()
                .id(1L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.BASIC)
                .grantedById(300L)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();

        when(userStorageAccessRepository.findById(1L))
                .thenReturn(Mono.just(testAccess));

        StepVerifier.create(userStorageAccessService.update(1L, update))
                .expectErrorMatches(throwable ->
                        throwable instanceof OperationNotAllowedException &&
                                throwable.getMessage().contains("Expiration date must be in the future")
                )
                .verify();

        verify(userStorageAccessRepository).findById(1L);
        verify(userStorageAccessRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowDuplicateUserStorageAccessException_WhenUserAndStorageChangedToExisting() {
        UserStorageAccess update = UserStorageAccess.builder()
                .id(1L)
                .userId(101L)
                .storageId(201L)
                .accessLevel(AccessLevel.BASIC)
                .grantedById(300L)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        when(userStorageAccessRepository.findById(1L))
                .thenReturn(Mono.just(testAccess));
        when(userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(101L, 201L, 1L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(userStorageAccessService.update(1L, update))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateUserStorageAccessException &&
                                throwable.getMessage().contains("User storage access already exists")
                )
                .verify();

        verify(userStorageAccessRepository).findById(1L);
        verify(userStorageAccessRepository).existsByUserIdAndStorageIdAndIdNot(101L, 201L, 1L);
        verify(userStorageAccessRepository, never()).save(any());
    }

    @Test
    void update_ShouldNotCheckDuplicate_WhenUserAndStorageNotChanged() {
        UserStorageAccess update = UserStorageAccess.builder()
                .id(1L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.ADMIN)
                .grantedById(300L)
                .expiresAt(LocalDateTime.now().plusDays(90))
                .isActive(false)
                .build();

        when(userStorageAccessRepository.findById(1L))
                .thenReturn(Mono.just(testAccess));
        when(userStorageAccessRepository.save(any(UserStorageAccess.class)))
                .thenReturn(Mono.just(update));

        StepVerifier.create(userStorageAccessService.update(1L, update))
                .verifyComplete();

        verify(userStorageAccessRepository).findById(1L);
        verify(userStorageAccessRepository, never()).existsByUserIdAndStorageIdAndIdNot(anyLong(), anyLong(), anyLong());
        verify(userStorageAccessRepository).save(any(UserStorageAccess.class));
    }

    @Test
    void delete_ShouldDeleteUserStorageAccess() {
        when(userStorageAccessRepository.deleteById(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(userStorageAccessService.delete(1L))
                .verifyComplete();

        verify(userStorageAccessRepository).deleteById(1L);
    }

    @Test
    void countUserStorageAccessesByFilters_ShouldReturnCorrectCount() {
        when(userStorageAccessRepository.countByFilters(100L, 200L, "MANAGER", true))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(userStorageAccessService.countUserStorageAccessesByFilters(100L, 200L, "MANAGER", true))
                .expectNext(1L)
                .verifyComplete();

        verify(userStorageAccessRepository).countByFilters(100L, 200L, "MANAGER", true);
    }

    @Test
    void findByUserAndStorage_ShouldReturnAccess_WhenExists() {
        when(userStorageAccessRepository.findByUserIdAndStorageId(100L, 200L))
                .thenReturn(Mono.just(testAccess));

        StepVerifier.create(userStorageAccessService.findByUserAndStorage(100L, 200L))
                .assertNext(access -> {
                    assertEquals(testAccess.getId(), access.getId());
                    assertEquals(100L, access.getUserId());
                    assertEquals(200L, access.getStorageId());
                })
                .verifyComplete();

        verify(userStorageAccessRepository).findByUserIdAndStorageId(100L, 200L);
    }

    @Test
    void findByUserAndStorage_ShouldThrowUserStorageAccessNotFoundException_WhenNotFound() {
        when(userStorageAccessRepository.findByUserIdAndStorageId(999L, 999L))
                .thenReturn(Mono.empty());

        StepVerifier.create(userStorageAccessService.findByUserAndStorage(999L, 999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserStorageAccessNotFoundException &&
                                throwable.getMessage().contains("User storage access not found")
                )
                .verify();

        verify(userStorageAccessRepository).findByUserIdAndStorageId(999L, 999L);
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenValidAccessExists() {
        when(userStorageAccessRepository.findByUserIdAndStorageId(100L, 200L))
                .thenReturn(Mono.just(testAccess));

        StepVerifier.create(userStorageAccessService.hasAccess(100L, 200L, AccessLevel.BASIC))
                .expectNext(true)
                .verifyComplete();

        verify(userStorageAccessRepository).findByUserIdAndStorageId(100L, 200L);
    }



    @Test
    void hasAccess_ShouldReturnFalse_WhenAccessInactive() {
        UserStorageAccess inactive = UserStorageAccess.builder()
                .id(4L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.ADMIN)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(5))
                .expiresAt(LocalDateTime.now().plusDays(25))
                .isActive(false)
                .build();

        when(userStorageAccessRepository.findByUserIdAndStorageId(100L, 200L))
                .thenReturn(Mono.just(inactive));

        StepVerifier.create(userStorageAccessService.hasAccess(100L, 200L, AccessLevel.BASIC))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenAccessExpired() {
        UserStorageAccess expired = UserStorageAccess.builder()
                .id(3L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.ADMIN)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(10))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();

        when(userStorageAccessRepository.findByUserIdAndStorageId(100L, 200L))
                .thenReturn(Mono.just(expired));

        StepVerifier.create(userStorageAccessService.hasAccess(100L, 200L, AccessLevel.BASIC))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenHigherAccessLevel() {
        when(userStorageAccessRepository.findByUserIdAndStorageId(100L, 200L))
                .thenReturn(Mono.just(testAccess));

        StepVerifier.create(userStorageAccessService.hasAccess(100L, 200L, AccessLevel.BASIC))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenLowerAccessLevel() {
        UserStorageAccess basicAccess = UserStorageAccess.builder()
                .id(1L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.BASIC)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        when(userStorageAccessRepository.findByUserIdAndStorageId(100L, 200L))
                .thenReturn(Mono.just(basicAccess));

        StepVerifier.create(userStorageAccessService.hasAccess(100L, 200L, AccessLevel.MANAGER))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deactivate_ShouldDeactivateAccess() {
        UserStorageAccess activeAccess = UserStorageAccess.builder()
                .id(1L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.MANAGER)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        UserStorageAccess deactivatedAccess = UserStorageAccess.builder()
                .id(1L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.MANAGER)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(false)
                .build();

        when(userStorageAccessRepository.findById(1L))
                .thenReturn(Mono.just(activeAccess));
        when(userStorageAccessRepository.save(any(UserStorageAccess.class)))
                .thenReturn(Mono.just(deactivatedAccess));

        StepVerifier.create(userStorageAccessService.deactivate(1L))
                .assertNext(access -> {
                    assertFalse(access.getIsActive());
                    assertEquals(1L, access.getId());
                })
                .verifyComplete();

        verify(userStorageAccessRepository).findById(1L);
        verify(userStorageAccessRepository).save(argThat(savedAccess -> !savedAccess.getIsActive()));
    }

    @Test
    void activate_ShouldActivateAccess() {
        UserStorageAccess inactiveAccess = UserStorageAccess.builder()
                .id(4L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.MANAGER)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(false)
                .build();

        UserStorageAccess activatedAccess = UserStorageAccess.builder()
                .id(4L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.MANAGER)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        when(userStorageAccessRepository.findById(4L))
                .thenReturn(Mono.just(inactiveAccess));
        when(userStorageAccessRepository.save(any(UserStorageAccess.class)))
                .thenReturn(Mono.just(activatedAccess));

        StepVerifier.create(userStorageAccessService.activate(4L))
                .assertNext(access -> {
                    assertTrue(access.getIsActive());
                    assertEquals(4L, access.getId());
                })
                .verifyComplete();

        verify(userStorageAccessRepository).findById(4L);
        verify(userStorageAccessRepository).save(argThat(savedAccess -> savedAccess.getIsActive()));
    }


    @Test
    void deactivateExpiredAccesses_ShouldDeactivateAllExpired() {
        List<UserStorageAccess> expiredAccesses = List.of(expiredAccess);
        when(userStorageAccessRepository.findExpiredAccesses(any(LocalDateTime.class)))
                .thenReturn(Flux.fromIterable(expiredAccesses));
        when(userStorageAccessRepository.save(any(UserStorageAccess.class)))
                .thenReturn(Mono.just(expiredAccess));

        StepVerifier.create(userStorageAccessService.deactivateExpiredAccesses())
                .verifyComplete();

        verify(userStorageAccessRepository).findExpiredAccesses(any(LocalDateTime.class));
        verify(userStorageAccessRepository).save(argThat(access -> !access.getIsActive()));
    }

    @Test
    void countActiveAccessesByUser_ShouldReturnCorrectCount() {
        when(userStorageAccessRepository.countByUserIdAndIsActive(100L, true))
                .thenReturn(Mono.just(2L));

        StepVerifier.create(userStorageAccessService.countActiveAccessesByUser(100L))
                .expectNext(2L)
                .verifyComplete();

        verify(userStorageAccessRepository).countByUserIdAndIsActive(100L, true);
    }

    @Test
    void countActiveAccessesByStorage_ShouldReturnCorrectCount() {
        when(userStorageAccessRepository.countByStorageIdAndIsActive(200L, true))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(userStorageAccessService.countActiveAccessesByStorage(200L))
                .expectNext(1L)
                .verifyComplete();

        verify(userStorageAccessRepository).countByStorageIdAndIsActive(200L, true);
    }

    @Test
    void findByUserAndStorage_ShouldThrowException_WhenNotFound() {
        when(userStorageAccessRepository.findByUserIdAndStorageId(999L, 999L))
                .thenReturn(Mono.empty());

        StepVerifier.create(userStorageAccessService.findByUserAndStorage(999L, 999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserStorageAccessNotFoundException &&
                                throwable.getMessage().contains("User storage access not found for user ID: 999")
                )
                .verify();

        verify(userStorageAccessRepository).findByUserIdAndStorageId(999L, 999L);
    }

    @Test
    void activate_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;
        when(userStorageAccessRepository.findById(nonExistentId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userStorageAccessService.activate(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserStorageAccessNotFoundException &&
                                throwable.getMessage().contains("User storage access not found with ID: " + nonExistentId)
                )
                .verify();

        verify(userStorageAccessRepository).findById(nonExistentId);
    }

    @Test
    void deactivate_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;
        when(userStorageAccessRepository.findById(nonExistentId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userStorageAccessService.deactivate(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserStorageAccessNotFoundException &&
                                throwable.getMessage().contains("User storage access not found with ID: " + nonExistentId)
                )
                .verify();

        verify(userStorageAccessRepository).findById(nonExistentId);
    }

    @Test
    void update_ShouldThrowException_WhenNotFound() {
        Long nonExistentId = 999L;
        UserStorageAccess update = UserStorageAccess.builder()
                .id(nonExistentId)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.BASIC)
                .grantedById(300L)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        when(userStorageAccessRepository.findById(nonExistentId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userStorageAccessService.update(nonExistentId, update))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserStorageAccessNotFoundException &&
                                throwable.getMessage().contains("User storage access not found with ID: " + nonExistentId)
                )
                .verify();

        verify(userStorageAccessRepository).findById(nonExistentId);
    }

    @Test
    void findUserStorageAccessesByFilters_ShouldHandleNullFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        List<UserStorageAccess> allAccesses = List.of(testAccess, testAccess2, expiredAccess, inactiveAccess);

        when(userStorageAccessRepository.findByUserIdAndStorageIdAndAccessLevel(null, null, null, null, pageable))
                .thenReturn(Flux.fromIterable(allAccesses));

        StepVerifier.create(userStorageAccessService.findUserStorageAccessesByFilters(null, null, null, null, pageable))
                .expectNextCount(4)
                .verifyComplete();

        verify(userStorageAccessRepository).findByUserIdAndStorageIdAndAccessLevel(null, null, null, null, pageable);
    }

    @Test
    void countUserStorageAccessesByFilters_ShouldHandleNullFilters() {
        when(userStorageAccessRepository.countByFilters(null, null, null, null))
                .thenReturn(Mono.just(4L));

        StepVerifier.create(userStorageAccessService.countUserStorageAccessesByFilters(null, null, null, null))
                .expectNext(4L)
                .verifyComplete();

        verify(userStorageAccessRepository).countByFilters(null, null, null, null);
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenEqualAccessLevel() {
        UserStorageAccess accessWithExactLevel = UserStorageAccess.builder()
                .id(1L)
                .userId(100L)
                .storageId(200L)
                .accessLevel(AccessLevel.MANAGER)
                .grantedById(300L)
                .grantedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        when(userStorageAccessRepository.findByUserIdAndStorageId(100L, 200L))
                .thenReturn(Mono.just(accessWithExactLevel));

        StepVerifier.create(userStorageAccessService.hasAccess(100L, 200L, AccessLevel.MANAGER))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void deactivateExpiredAccesses_ShouldNotSave_WhenNoExpiredAccesses() {
        when(userStorageAccessRepository.findExpiredAccesses(any(LocalDateTime.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(userStorageAccessService.deactivateExpiredAccesses())
                .verifyComplete();

        verify(userStorageAccessRepository).findExpiredAccesses(any(LocalDateTime.class));
        verify(userStorageAccessRepository, never()).save(any());
    }
}