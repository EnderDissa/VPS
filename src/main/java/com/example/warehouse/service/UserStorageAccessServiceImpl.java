package com.example.warehouse.service;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.enumeration.AccessLevel;
import com.example.warehouse.exception.UserStorageAccessNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.DuplicateUserStorageAccessException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.mapper.UserStorageAccessMapper;
import com.example.warehouse.repository.UserStorageAccessRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.service.interfaces.StorageService;
import com.example.warehouse.service.interfaces.UserService;
import com.example.warehouse.service.interfaces.UserStorageAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service

@RequiredArgsConstructor
public class UserStorageAccessServiceImpl implements UserStorageAccessService {

    private final UserStorageAccessRepository userStorageAccessRepository;
    private final UserService userService;
    private final StorageService storageService;

    @Override
    public UserStorageAccess create(UserStorageAccess userStorageAccess) {

        User user = userService.getUserById(userStorageAccess.getUser().getId());

        Storage storage = storageService.getById(userStorageAccess.getStorage().getId());

        User grantedBy = userService.getUserById(userStorageAccess.getGrantedBy().getId());

        if (userStorageAccessRepository.existsByUserIdAndStorageId(userStorageAccess.getUser().getId(), userStorageAccess.getStorage().getId())) {
            throw new DuplicateUserStorageAccessException(
                    "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                            " and storage ID: " + userStorageAccess.getStorage().getId());
        }

        if (userStorageAccess.getExpiresAt() != null && userStorageAccess.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OperationNotAllowedException("Expiration date must be in the future");
        }


        userStorageAccess.setUser(user);
        userStorageAccess.setStorage(storage);
        userStorageAccess.setGrantedBy(grantedBy);
        userStorageAccess.setGrantedAt(LocalDateTime.now());

        UserStorageAccess savedAccess = userStorageAccessRepository.save(userStorageAccess);
        log.info("User storage access created successfully with ID: {}", savedAccess.getId());

        return savedAccess;
    }

    @Override
    public UserStorageAccess getById(Long id) {
        log.debug("Fetching user storage access by ID: {}", id);

        return userStorageAccessRepository.findById(id)
                .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));
    }

    @Override
    public void update(Long id, UserStorageAccess userStorageAccess) {
        log.info("Updating user storage access with ID: {}", id);

        try {
            UserStorageAccess existingAccess = userStorageAccessRepository.findById(id)
                    .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));

            if (userStorageAccess.getExpiresAt() != null && userStorageAccess.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new OperationNotAllowedException("Expiration date must be in the future");
            }

            updateRelatedEntities(existingAccess, userStorageAccess);

            existingAccess.setAccessLevel(userStorageAccess.getAccessLevel());
            existingAccess.setExpiresAt(userStorageAccess.getExpiresAt());
            existingAccess.setIsActive(userStorageAccess.getIsActive());

            userStorageAccessRepository.save(existingAccess);
            log.info("User storage access with ID: {} updated successfully", id);

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("uk_user_storage_access")) {
                throw new DuplicateUserStorageAccessException(
                        "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                                " and storage ID: " + userStorageAccess.getStorage().getId());
            }
            throw e;
        }
    }

    @Override
    
    public void delete(Long id) {
        log.info("Deleting user storage access with ID: {}", id);

        if (!userStorageAccessRepository.existsById(id)) {
            throw new UserStorageAccessNotFoundException("User storage access not found with ID: " + id);
        }

        userStorageAccessRepository.deleteById(id);
        log.info("User storage access with ID: {} deleted successfully", id);
    }

    @Override
    public Page<UserStorageAccess> findPage(int page, int size, Long userId, Long storageId,
                                               AccessLevel accessLevel, Boolean active) {
        log.debug("Fetching user storage access page - page: {}, size: {}, userId: {}, storageId: {}, accessLevel: {}, active: {}",
                page, size, userId, storageId, accessLevel, active);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "grantedAt"));

        Page<UserStorageAccess> accessPage;

        if (userId != null && storageId != null && accessLevel != null && active != null) {
            accessPage = userStorageAccessRepository.findByUserIdAndStorageIdAndAccessLevelAndIsActive(
                    userId, storageId, accessLevel, active, pageable);
        } else if (userId != null && storageId != null && accessLevel != null) {
            accessPage = userStorageAccessRepository.findByUserIdAndStorageIdAndAccessLevel(
                    userId, storageId, accessLevel, pageable);
        } else if (userId != null && storageId != null && active != null) {
            accessPage = userStorageAccessRepository.findByUserIdAndStorageIdAndIsActive(
                    userId, storageId, active, pageable);
        } else if (userId != null && accessLevel != null && active != null) {
            accessPage = userStorageAccessRepository.findByUserIdAndAccessLevelAndIsActive(
                    userId, accessLevel, active, pageable);
        } else if (storageId != null && accessLevel != null && active != null) {
            accessPage = userStorageAccessRepository.findByStorageIdAndAccessLevelAndIsActive(
                    storageId, accessLevel, active, pageable);
        } else if (userId != null && storageId != null) {
            accessPage = userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId, pageable);
        } else if (userId != null && accessLevel != null) {
            accessPage = userStorageAccessRepository.findByUserIdAndAccessLevel(userId, accessLevel, pageable);
        } else if (userId != null && active != null) {
            accessPage = userStorageAccessRepository.findByUserIdAndIsActive(userId, active, pageable);
        } else if (storageId != null && accessLevel != null) {
            accessPage = userStorageAccessRepository.findByStorageIdAndAccessLevel(storageId, accessLevel, pageable);
        } else if (storageId != null && active != null) {
            accessPage = userStorageAccessRepository.findByStorageIdAndIsActive(storageId, active, pageable);
        } else if (accessLevel != null && active != null) {
            accessPage = userStorageAccessRepository.findByAccessLevelAndIsActive(accessLevel, active, pageable);
        } else if (userId != null) {
            accessPage = userStorageAccessRepository.findByUserId(userId, pageable);
        } else if (storageId != null) {
            accessPage = userStorageAccessRepository.findByStorageId(storageId, pageable);
        } else if (accessLevel != null) {
            accessPage = userStorageAccessRepository.findByAccessLevel(accessLevel, pageable);
        } else if (active != null) {
            accessPage = userStorageAccessRepository.findByIsActive(active, pageable);
        } else {
            accessPage = userStorageAccessRepository.findAll(pageable);
        }

        return accessPage;
    }

    private void updateRelatedEntities(UserStorageAccess access, UserStorageAccess userStorageAccess) {
        if (!access.getUser().getId().equals(userStorageAccess.getUser().getId())) {
            User user = userService.getUserById(userStorageAccess.getUser().getId());
            access.setUser(user);

            if (userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(
                    userStorageAccess.getUser().getId(), userStorageAccess.getStorage().getId(), access.getId())) {
                throw new DuplicateUserStorageAccessException(
                        "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                                " and storage ID: " + userStorageAccess.getStorage().getId());
            }
        }

        if (!access.getStorage().getId().equals(userStorageAccess.getStorage().getId())) {
            Storage storage = storageService.getById(userStorageAccess.getStorage().getId());
            access.setStorage(storage);

            if (userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(
                    userStorageAccess.getUser().getId(), userStorageAccess.getStorage().getId(), access.getId())) {
                throw new DuplicateUserStorageAccessException(
                        "User storage access already exists for user ID: " + userStorageAccess.getUser().getId() +
                                " and storage ID: " + userStorageAccess.getStorage().getId());
            }
        }

        if (!access.getGrantedBy().getId().equals(userStorageAccess.getGrantedBy().getId())) {
            User grantedBy = userService.getUserById(userStorageAccess.getGrantedBy().getId());
            access.setGrantedBy(grantedBy);
        }
    }


    public UserStorageAccess findByUserAndStorage(Long userId, Long storageId) {
        log.debug("Finding user storage access by userId: {} and storageId: {}", userId, storageId);

        return userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId)
                .orElseThrow(() -> new UserStorageAccessNotFoundException(
                        "User storage access not found for user ID: " + userId + " and storage ID: " + storageId));
    }


    public boolean hasAccess(Long userId, Long storageId, AccessLevel requiredLevel) {
        log.debug("Checking access for userId: {}, storageId: {}, requiredLevel: {}", userId, storageId, requiredLevel);

        UserStorageAccess access = userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId)
                .orElse(null);

        if (access == null || !access.getIsActive()) {
            return false;
        }

        if (access.getExpiresAt() != null && access.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        return isAccessLevelSufficient(access.getAccessLevel(), requiredLevel);
    }


    public UserStorageAccess deactivate(Long id) {
        log.info("Deactivating user storage access with ID: {}", id);

        UserStorageAccess access = userStorageAccessRepository.findById(id)
                .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));

        access.setIsActive(false);
        UserStorageAccess updatedAccess = userStorageAccessRepository.save(access);

        log.info("User storage access with ID: {} deactivated successfully", id);
        return updatedAccess;
    }


    public UserStorageAccess activate(Long id) {
        log.info("Activating user storage access with ID: {}", id);

        UserStorageAccess access = userStorageAccessRepository.findById(id)
                .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));

        access.setIsActive(true);
        UserStorageAccess updatedAccess = userStorageAccessRepository.save(access);

        log.info("User storage access with ID: {} activated successfully", id);
        return updatedAccess;
    }


    public List<UserStorageAccess> findByUser(Long userId) {
        log.debug("Finding all user storage accesses for userId: {}", userId);

        return userStorageAccessRepository.findByUserId(userId);
    }


    public List<UserStorageAccess> findByStorage(Long storageId) {
        log.debug("Finding all user storage accesses for storageId: {}", storageId);

        return userStorageAccessRepository.findByStorageId(storageId);
    }


    public List<UserStorageAccess> findExpiredAccesses() {
        log.debug("Finding all expired user storage accesses");

        return userStorageAccessRepository.findExpiredAccesses(LocalDateTime.now());
    }


    public void deactivateExpiredAccesses() {
        log.info("Deactivating expired user storage accesses");

        List<UserStorageAccess> expiredAccesses = userStorageAccessRepository.findExpiredAccesses(LocalDateTime.now());

        for (UserStorageAccess access : expiredAccesses) {
            if (access.getIsActive()) {
                access.setIsActive(false);
                log.debug("Deactivated expired access with ID: {}", access.getId());
            }
        }

        userStorageAccessRepository.saveAll(expiredAccesses);
        log.info("Deactivated {} expired user storage accesses", expiredAccesses.size());
    }

    private boolean isAccessLevelSufficient(AccessLevel userLevel, AccessLevel requiredLevel) {
        return userLevel.ordinal() >= requiredLevel.ordinal();
    }

    public long countActiveAccessesByUser(Long userId) {
        log.debug("Counting active accesses for userId: {}", userId);
        return userStorageAccessRepository.countByUserIdAndIsActive(userId, true);
    }

    public long countActiveAccessesByStorage(Long storageId) {
        log.debug("Counting active accesses for storageId: {}", storageId);
        return userStorageAccessRepository.countByStorageIdAndIsActive(storageId, true);
    }
}
