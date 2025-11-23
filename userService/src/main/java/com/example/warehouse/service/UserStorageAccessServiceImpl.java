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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStorageAccessServiceImpl implements UserStorageAccessService {

    private final UserStorageAccessRepository userStorageAccessRepository;
    private final UserService userService;
    private final StorageServiceClient storageService;

    @Override
    public Mono<UserStorageAccess> create(UserStorageAccess userStorageAccess) {
        log.info("Creating new user storage access for user ID: {} and storage ID: {}", userStorageAccess.getUserId(), userStorageAccess.getStorageId());


        if (userStorageAccess.getExpiresAt() != null && userStorageAccess.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Mono.error(new OperationNotAllowedException("Expiration date must be in the future"));
        }


        return userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(userStorageAccess.getUserId(), userStorageAccess.getStorageId(), -1L)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateUserStorageAccessException(
                                "User storage access already exists for user ID: " + userStorageAccess.getUserId() +
                                        " and storage ID: " + userStorageAccess.getStorageId()));
                    }

                    userStorageAccess.setGrantedAt(LocalDateTime.now());
                    return userStorageAccessRepository.save(userStorageAccess);
                })
                .doOnSuccess(savedAccess -> log.info("User storage access created successfully with ID: {}", savedAccess.getId()));
    }

    @Override
    public Mono<UserStorageAccess> getById(Long id) {
        log.debug("Fetching user storage access by ID: {}", id);
        return userStorageAccessRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id)));
    }

    @Override
    public Mono<Void> update(Long id, UserStorageAccess userStorageAccess) {
        return userStorageAccessRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id)))
                .flatMap(existingAccess -> {
                    if (userStorageAccess.getExpiresAt() != null && userStorageAccess.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return Mono.error(new OperationNotAllowedException("Expiration date must be in the future"));
                    }


                    if (!existingAccess.getUserId().equals(userStorageAccess.getUserId()) || !existingAccess.getStorageId().equals(userStorageAccess.getStorageId())) {
                        return userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(
                                        userStorageAccess.getUserId(), userStorageAccess.getStorageId(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new DuplicateUserStorageAccessException(
                                                "User storage access already exists for user ID: " + userStorageAccess.getUserId() +
                                                        " and storage ID: " + userStorageAccess.getStorageId()));
                                    }
                                    return Mono.just(existingAccess);
                                });
                    } else {
                        return Mono.just(existingAccess);
                    }
                })
                .doOnNext(existingAccess -> {
                    existingAccess.setAccessLevel(userStorageAccess.getAccessLevel());
                    existingAccess.setExpiresAt(userStorageAccess.getExpiresAt());
                    existingAccess.setIsActive(userStorageAccess.getIsActive());
                })
                .flatMap(userStorageAccessRepository::save)
                .doOnSuccess(v -> log.info("User storage access with ID: {} updated successfully", id))
                .then();
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting user storage access with ID: {}", id);
        return userStorageAccessRepository.deleteById(id)
                .doOnSuccess(v -> log.info("User storage access with ID: {} deleted successfully", id));
    }

    @Override
    public Flux<UserStorageAccess> findUserStorageAccessesByFilters(Long userId, Long storageId, String accessLevel, Boolean active, Pageable pageable) {
        log.debug("Fetching user storage access page - pageable: {}, userId: {}, storageId: {}, accessLevel: {}, active: {}",
                pageable, userId, storageId, accessLevel, active);


        return userStorageAccessRepository.findByUserIdAndStorageIdAndAccessLevel(userId, storageId, accessLevel, active, pageable);
    }

    @Override
    public Mono<Long> countUserStorageAccessesByFilters(Long userId, Long storageId, String accessLevel, Boolean active) {
        log.debug("Counting user storage access - userId: {}, storageId: {}, accessLevel: {}, active: {}",
                userId, storageId, accessLevel, active);

        return userStorageAccessRepository.countByFilters(userId, storageId, accessLevel, active);
    }

    @Override
    public Mono<UserStorageAccess> findByUserAndStorage(Long userId, Long storageId) {
        log.debug("Finding user storage access by userId: {} and storageId: {}", userId, storageId);
        return userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId)
                .switchIfEmpty(Mono.error(new UserStorageAccessNotFoundException(
                        "User storage access not found for user ID: " + userId + " and storage ID: " + storageId)));
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

                    return access.getAccessLevel().ordinal() >= requiredLevel.ordinal();
                })
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<UserStorageAccess> deactivate(Long id) {
        log.info("Deactivating user storage access with ID: {}", id);
        return userStorageAccessRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id)))
                .doOnNext(access -> access.setIsActive(false))
                .flatMap(userStorageAccessRepository::save)
                .doOnSuccess(updatedAccess -> log.info("User storage access with ID: {} deactivated successfully", id));
    }

    @Override
    public Mono<UserStorageAccess> activate(Long id) {
        log.info("Activating user storage access with ID: {}", id);
        return userStorageAccessRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserStorageAccessNotFoundException("User storage access not found with ID: " + id)))
                .doOnNext(access -> access.setIsActive(true))
                .flatMap(userStorageAccessRepository::save)
                .doOnSuccess(updatedAccess -> log.info("User storage access with ID: {} activated successfully", id));
    }

    @Override
    public Flux<UserStorageAccess> findByUser(Long userId) {
        log.debug("Finding all user storage accesses for userId: {}", userId);
        return userStorageAccessRepository.findByUserId(userId);
    }

    @Override
    public Flux<UserStorageAccess> findByStorage(Long storageId) {
        log.debug("Finding all user storage accesses for storageId: {}", storageId);
        return userStorageAccessRepository.findByStorageId(storageId);
    }

    @Override
    public Flux<UserStorageAccess> findExpiredAccesses() {
        log.debug("Finding all expired user storage accesses");
        return userStorageAccessRepository.findExpiredAccesses(LocalDateTime.now());
    }

    @Override
    public Mono<Void> deactivateExpiredAccesses() {
        log.info("Deactivating expired user storage accesses");

        return findExpiredAccesses()
                .doOnNext(access -> {
                    if (access.getIsActive()) {
                        access.setIsActive(false);
                        log.debug("Deactivated expired access with ID: {}", access.getId());
                    }
                })
                .flatMap(userStorageAccessRepository::save)
                .then();
    }

    @Override
    public Mono<Long> countActiveAccessesByUser(Long userId) {
        log.debug("Counting active accesses for userId: {}", userId);
        return userStorageAccessRepository.countByUserIdAndIsActive(userId, true);
    }

    @Override
    public Mono<Long> countActiveAccessesByStorage(Long storageId) {
        log.debug("Counting active accesses for storageId: {}", storageId);
        return userStorageAccessRepository.countByStorageIdAndIsActive(storageId, true);
    }
}