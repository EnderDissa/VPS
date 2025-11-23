package com.example.warehouse.service;

import com.example.warehouse.client.StorageServiceClient;
import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import com.example.warehouse.exception.DuplicateUserStorageAccessException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.exception.UserStorageAccessNotFoundException;
import com.example.warehouse.repository.UserStorageAccessRepository;
import com.example.warehouse.service.interfaces.UserService;
import com.example.warehouse.service.interfaces.UserStorageAccessService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStorageAccessServiceImpl implements UserStorageAccessService {

    private final UserStorageAccessRepository userStorageAccessRepository;
    private final UserService userService;
    private final StorageServiceClient storageService;

    @Override
    public Mono<UserStorageAccess> create(UserStorageAccess userStorageAccess) {
        log.info("Creating new user storage access for user ID: {} and storage ID: {}", userStorageAccess.getUser().getId(), userStorageAccess.getStorage().getId());

        return userService.getUserById(userStorageAccess.getUser().getId())
                .flatMap(user -> storageService.getById(userStorageAccess.getStorage().getId())
                        .flatMap(storage -> userService.getUserById(userStorageAccess.getGrantedBy().getId())
                                .flatMap(grantedBy -> {
                                    if (userStorageAccess.getExpiresAt() != null && userStorageAccess.getExpiresAt().isBefore(LocalDateTime.now())) {
                                        return Mono.error(new OperationNotAllowedException("Expiration date must be in the future"));
                                    }

                                    // Check for duplicate using repository method (assuming it exists and returns Mono<Boolean>)
                                    // If not, you might need to use findById or similar and check the result
                                    return Mono.fromCallable(() -> userStorageAccessRepository.existsByUserIdAndStorageId(
                                                    userStorageAccess.getUser().getId(), userStorageAccess.getStorage().getId()))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .flatMap(exists -> {
                                                if (exists) {
                                                    return Mono.error(new DuplicateUserStorageAccessException(
                                                            "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                                                                    " and storage ID: " + userStorageAccess.getStorage().getId()));
                                                }

                                                userStorageAccess.setUser(user);
                                                userStorageAccess.setStorage(storage);
                                                userStorageAccess.setGrantedBy(grantedBy);
                                                userStorageAccess.setGrantedAt(LocalDateTime.now());

                                                return Mono.fromCallable(() -> userStorageAccessRepository.save(userStorageAccess))
                                                        .subscribeOn(Schedulers.boundedElastic());
                                            });
                                })));
    }

    @Override
    public Mono<UserStorageAccess> getById(Long id) {
        log.debug("Fetching user storage access by ID: {}", id);

        return Mono.fromCallable(() -> userStorageAccessRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id))));
    }

    @Override
    public Mono<Void> update(Long id, UserStorageAccess userStorageAccess) {
        log.info("Updating user storage access with ID: {}", id);

        return Mono.fromCallable(() -> userStorageAccessRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));
                    }

                    UserStorageAccess existingAccess = optional.get();

                    if (userStorageAccess.getExpiresAt() != null && userStorageAccess.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return Mono.error(new OperationNotAllowedException("Expiration date must be in the future"));
                    }

                    return updateRelatedEntities(existingAccess, userStorageAccess)
                            .doOnNext(v -> {
                                existingAccess.setAccessLevel(userStorageAccess.getAccessLevel());
                                existingAccess.setExpiresAt(userStorageAccess.getExpiresAt());
                                existingAccess.setIsActive(userStorageAccess.getIsActive());
                            })
                            .then(Mono.fromCallable(() -> userStorageAccessRepository.save(existingAccess))
                                    .subscribeOn(Schedulers.boundedElastic()));
                })
                .onErrorResume(DataIntegrityViolationException.class, ex -> {
                    if (ex.getMessage() != null && ex.getMessage().contains("uk_user_storage_access")) {
                        return Mono.error(new DuplicateUserStorageAccessException(
                                "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                                        " and storage ID: " + userStorageAccess.getStorage().getId()));
                    }
                    return Mono.error(ex);
                })
                .doOnSuccess(v -> log.info("User storage access with ID: {} updated successfully", id))
                .then();
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting user storage access with ID: {}", id);

        return Mono.fromCallable(() -> userStorageAccessRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));
                    }
                    return Mono.fromCallable(() -> {
                        userStorageAccessRepository.deleteById(id);
                        return null;
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("User storage access with ID: {} deleted successfully", id))
                .then();
    }

    // New methods for reactive pagination
    @Override
    public Flux<UserStorageAccess> findAccessByFilters(Long userId, Long storageId, AccessLevel accessLevel, Boolean active, Pageable pageable) {
        log.debug("Fetching user storage access page - pageable: {}, userId: {}, storageId: {}, accessLevel: {}, active: {}",
                pageable, userId, storageId, accessLevel, active);

        return Mono.fromCallable(() -> {
                    if (userId != null && storageId != null && accessLevel != null && active != null) {
                        return userStorageAccessRepository.findByUserIdAndStorageIdAndAccessLevelAndIsActive(
                                userId, storageId, accessLevel, active, pageable);
                    } else if (userId != null && storageId != null && accessLevel != null) {
                        return userStorageAccessRepository.findByUserIdAndStorageIdAndAccessLevel(
                                userId, storageId, accessLevel, pageable);
                    } else if (userId != null && storageId != null && active != null) {
                        return userStorageAccessRepository.findByUserIdAndStorageIdAndIsActive(
                                userId, storageId, active, pageable);
                    } else if (userId != null && accessLevel != null && active != null) {
                        return userStorageAccessRepository.findByUserIdAndAccessLevelAndIsActive(
                                userId, accessLevel, active, pageable);
                    } else if (storageId != null && accessLevel != null && active != null) {
                        return userStorageAccessRepository.findByStorageIdAndAccessLevelAndIsActive(
                                storageId, accessLevel, active, pageable);
                    } else if (userId != null && storageId != null) {
                        return userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId, pageable);
                    } else if (userId != null && accessLevel != null) {
                        return userStorageAccessRepository.findByUserIdAndAccessLevel(userId, accessLevel, pageable);
                    } else if (userId != null && active != null) {
                        return userStorageAccessRepository.findByUserIdAndIsActive(userId, active, pageable);
                    } else if (storageId != null && accessLevel != null) {
                        return userStorageAccessRepository.findByStorageIdAndAccessLevel(storageId, accessLevel, pageable);
                    } else if (storageId != null && active != null) {
                        return userStorageAccessRepository.findByStorageIdAndIsActive(storageId, active, pageable);
                    } else if (accessLevel != null && active != null) {
                        return userStorageAccessRepository.findByAccessLevelAndIsActive(accessLevel, active, pageable);
                    } else if (userId != null) {
                        return userStorageAccessRepository.findByUserId(userId, pageable);
                    } else if (storageId != null) {
                        return userStorageAccessRepository.findByStorageId(storageId, pageable);
                    } else if (accessLevel != null) {
                        return userStorageAccessRepository.findByAccessLevel(accessLevel, pageable);
                    } else if (active != null) {
                        return userStorageAccessRepository.findByIsActive(active, pageable);
                    } else {
                        return userStorageAccessRepository.findAll(pageable);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    @Override
    public Mono<Long> countAccessByFilters(Long userId, Long storageId, AccessLevel accessLevel, Boolean active) {
        log.debug("Counting user storage access - userId: {}, storageId: {}, accessLevel: {}, active: {}",
                userId, storageId, accessLevel, active);

        return Mono.fromCallable(() -> {
                    if (userId != null && storageId != null && accessLevel != null && active != null) {
                        return userStorageAccessRepository.countByUserIdAndStorageIdAndAccessLevelAndIsActive(userId, storageId, accessLevel, active);
                    } else if (userId != null && storageId != null && accessLevel != null) {
                        return userStorageAccessRepository.countByUserIdAndStorageIdAndAccessLevel(userId, storageId, accessLevel);
                    } else if (userId != null && storageId != null && active != null) {
                        return userStorageAccessRepository.countByUserIdAndStorageIdAndIsActive(userId, storageId, active);
                    } else if (userId != null && accessLevel != null && active != null) {
                        return userStorageAccessRepository.countByUserIdAndAccessLevelAndIsActive(userId, accessLevel, active);
                    } else if (storageId != null && accessLevel != null && active != null) {
                        return userStorageAccessRepository.countByStorageIdAndAccessLevelAndIsActive(storageId, accessLevel, active);
                    } else if (userId != null && storageId != null) {
                        return userStorageAccessRepository.countByUserIdAndStorageId(userId, storageId);
                    } else if (userId != null && accessLevel != null) {
                        return userStorageAccessRepository.countByUserIdAndAccessLevel(userId, accessLevel);
                    } else if (userId != null && active != null) {
                        return userStorageAccessRepository.countByUserIdAndIsActive(userId, active);
                    } else if (storageId != null && accessLevel != null) {
                        return userStorageAccessRepository.countByStorageIdAndAccessLevel(storageId, accessLevel);
                    } else if (storageId != null && active != null) {
                        return userStorageAccessRepository.countByStorageIdAndIsActive(storageId, active);
                    } else if (accessLevel != null && active != null) {
                        return userStorageAccessRepository.countByAccessLevelAndIsActive(accessLevel, active);
                    } else if (userId != null) {
                        return userStorageAccessRepository.countByUserId(userId);
                    } else if (storageId != null) {
                        return userStorageAccessRepository.countByStorageId(storageId);
                    } else if (accessLevel != null) {
                        return userStorageAccessRepository.countByAccessLevel(accessLevel);
                    } else if (active != null) {
                        return userStorageAccessRepository.countByIsActive(active);
                    } else {
                        return userStorageAccessRepository.count();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Helper method for updating related entities
    private Mono<Void> updateRelatedEntities(UserStorageAccess access, UserStorageAccess userStorageAccess) {
        Mono<Void> userUpdate = Mono.empty();
        if (!access.getUser().getId().equals(userStorageAccess.getUser().getId())) {
            userUpdate = userService.getUserById(userStorageAccess.getUser().getId())
                    .doOnNext(user -> {
                        access.setUser(user);

                        // Check for duplicate again after user change
                        if (userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(
                                userStorageAccess.getUser().getId(), userStorageAccess.getStorage().getId(), access.getId())) {
                            throw new DuplicateUserStorageAccessException(
                                    "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                                            " and storage ID: " + userStorageAccess.getStorage().getId());
                        }
                    })
                    .then();
        }

        Mono<Void> storageUpdate = Mono.empty();
        if (!access.getStorage().getId().equals(userStorageAccess.getStorage().getId())) {
            storageUpdate = storageService.getById(userStorageAccess.getStorage().getId())
                    .doOnNext(storage -> {
                        access.setStorage(storage);

                        // Check for duplicate again after storage change
                        if (userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(
                                userStorageAccess.getUser().getId(), userStorageAccess.getStorage().getId(), access.getId())) {
                            throw new DuplicateUserStorageAccessException(
                                    "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                                            " and storage ID: " + userStorageAccess.getStorage().getId());
                        }
                    })
                    .then();
        }

        Mono<Void> grantedByUpdate = Mono.empty();
        if (!access.getGrantedBy().getId().equals(userStorageAccess.getGrantedBy().getId())) {
            grantedByUpdate = userService.getUserById(userStorageAccess.getGrantedBy().getId())
                    .doOnNext(grantedBy -> access.setGrantedBy(grantedBy))
                    .then();
        }

        return userUpdate.then(storageUpdate).then(grantedByUpdate);
    }

    @Override
    public Mono<UserStorageAccess> findByUserAndStorage(Long userId, Long storageId) {
        log.debug("Finding user storage access by userId: {} and storageId: {}", userId, storageId);

        return Mono.fromCallable(() -> userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new UserStorageAccessNotFoundException(
                                "User storage access not found for user ID: " + userId + " and storage ID: " + storageId))));
    }

    @Override
    public Mono<Boolean> hasAccess(Long userId, Long storageId, AccessLevel requiredLevel) {
        log.debug("Checking access for userId: {}, storageId: {}, requiredLevel: {}", userId, storageId, requiredLevel);

        return findByUserAndStorage(userId, storageId)
                .map(access -> {
                    if (!access.getIsActive()) {
                        return false;
                    }

                    if (access.getExpiresAt() != null && access.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return false;
                    }

                    return isAccessLevelSufficient(access.getAccessLevel(), requiredLevel);
                })
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<UserStorageAccess> deactivate(Long id) {
        log.info("Deactivating user storage access with ID: {}", id);

        return Mono.fromCallable(() -> userStorageAccessRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));
                    }

                    UserStorageAccess access = optional.get();
                    access.setIsActive(false);

                    return Mono.fromCallable(() -> userStorageAccessRepository.save(access))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(updated -> log.info("User storage access with ID: {} deactivated successfully", id));
    }

    @Override
    public Mono<UserStorageAccess> activate(Long id) {
        log.info("Activating user storage access with ID: {}", id);

        return Mono.fromCallable(() -> userStorageAccessRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));
                    }

                    UserStorageAccess access = optional.get();
                    access.setIsActive(true);

                    return Mono.fromCallable(() -> userStorageAccessRepository.save(access))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(updated -> log.info("User storage access with ID: {} activated successfully", id));
    }

    @Override
    public Flux<UserStorageAccess> findByUser(Long userId) {
        log.debug("Finding all user storage accesses for userId: {}", userId);

        return Flux.fromIterable(userStorageAccessRepository.findByUserId(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<UserStorageAccess> findByStorage(Long storageId) {
        log.debug("Finding all user storage accesses for storageId: {}", storageId);

        return Flux.fromIterable(userStorageAccessRepository.findByStorageId(storageId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<UserStorageAccess> findExpiredAccesses() {
        log.debug("Finding all expired user storage accesses");

        return Flux.fromIterable(userStorageAccessRepository.findExpiredAccesses(LocalDateTime.now()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deactivateExpiredAccesses() {
        log.info("Deactivating expired user storage accesses");

        return findExpiredAccesses()
                .collectList()
                .flatMap(expiredAccesses -> {
                    for (UserStorageAccess access : expiredAccesses) {
                        if (access.getIsActive()) {
                            access.setIsActive(false);
                            log.debug("Deactivated expired access with ID: {}", access.getId());
                        }
                    }

                    return Mono.fromCallable(() -> {
                        if (!expiredAccesses.isEmpty()) {
                            userStorageAccessRepository.saveAll(expiredAccesses);
                        }
                        return null;
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Deactivated {} expired user storage accesses", "count unknown without a separate step")).then();
    }

    private boolean isAccessLevelSufficient(AccessLevel userLevel, AccessLevel requiredLevel) {
        return userLevel.ordinal() >= requiredLevel.ordinal();
    }

    @Override
    public Mono<Long> countActiveAccessesByUser(Long userId) {
        log.debug("Counting active accesses for userId: {}", userId);
        return Mono.fromCallable(() -> userStorageAccessRepository.countByUserIdAndIsActive(userId, true))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> countActiveAccessesByStorage(Long storageId) {
        log.debug("Counting active accesses for storageId: {}", storageId);
        return Mono.fromCallable(() -> userStorageAccessRepository.countByStorageIdAndIsActive(storageId, true))
                .subscribeOn(Schedulers.boundedElastic());
    }
}