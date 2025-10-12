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
import com.example.warehouse.service.interfaces.UserStorageAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Transactional
@RequiredArgsConstructor
public class UserStorageAccessServiceImpl implements UserStorageAccessService {

    private final UserStorageAccessRepository userStorageAccessRepository;
    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final UserStorageAccessMapper userStorageAccessMapper;

    @Override
    @Transactional
    public UserStorageAccessDTO create(UserStorageAccessDTO dto) {
        log.info("Creating new user storage access - userId: {}, storageId: {}, accessLevel: {}",
                dto.getUserId(), dto.getStorageId(), dto.getAccessLevel());

        // Проверяем существование user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.getUserId()));

        // Проверяем существование storage
        Storage storage = storageRepository.findById(dto.getStorageId())
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + dto.getStorageId()));

        // Проверяем существование grantedBy user
        User grantedBy = userRepository.findById(dto.getGrantedById())
                .orElseThrow(() -> new UserNotFoundException("Granted by user not found with ID: " + dto.getGrantedById()));

        // Проверяем уникальность комбинации user + storage
        if (userStorageAccessRepository.existsByUserIdAndStorageId(dto.getUserId(), dto.getStorageId())) {
            throw new DuplicateUserStorageAccessException(
                    "User storage access already exists for user ID: " + dto.getUserId() +
                            " and storage ID: " + dto.getStorageId());
        }

        // Проверяем expiration date
        if (dto.getExpiresAt() != null && dto.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OperationNotAllowedException("Expiration date must be in the future");
        }

        // Создаем entity
        UserStorageAccess access = userStorageAccessMapper.toEntity(dto);
        access.setUser(user);
        access.setStorage(storage);
        access.setGrantedBy(grantedBy);
        access.setGrantedAt(LocalDateTime.now());

        // Сохраняем
        UserStorageAccess savedAccess = userStorageAccessRepository.save(access);
        log.info("User storage access created successfully with ID: {}", savedAccess.getId());

        return userStorageAccessMapper.toDTO(savedAccess);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStorageAccessDTO getById(Long id) {
        log.debug("Fetching user storage access by ID: {}", id);

        UserStorageAccess access = userStorageAccessRepository.findById(id)
                .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));

        return userStorageAccessMapper.toDTO(access);
    }

    @Override
    @Transactional
    public void update(Long id, UserStorageAccessDTO dto) {
        log.info("Updating user storage access with ID: {}", id);

        // Находим существующую запись
        UserStorageAccess existingAccess = userStorageAccessRepository.findById(id)
                .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));

        // Проверяем expiration date
        if (dto.getExpiresAt() != null && dto.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OperationNotAllowedException("Expiration date must be in the future");
        }

        // Проверяем и обновляем связанные сущности
        updateRelatedEntities(existingAccess, dto);

        // Обновляем остальные поля
        existingAccess.setAccessLevel(dto.getAccessLevel());
        existingAccess.setExpiresAt(dto.getExpiresAt());
        existingAccess.setIsActive(dto.getIsActive());

        userStorageAccessRepository.save(existingAccess);
        log.info("User storage access with ID: {} updated successfully", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting user storage access with ID: {}", id);

        if (!userStorageAccessRepository.existsById(id)) {
            throw new UserStorageAccessNotFoundException("User storage access not found with ID: " + id);
        }

        userStorageAccessRepository.deleteById(id);
        log.info("User storage access with ID: {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStorageAccessDTO> findPage(int page, int size, Long userId, Long storageId,
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

        return accessPage.map(userStorageAccessMapper::toDTO);
    }

    // Вспомогательные методы

    private void updateRelatedEntities(UserStorageAccess access, UserStorageAccessDTO dto) {
        // Обновляем user если изменился
        if (!access.getUser().getId().equals(dto.getUserId())) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.getUserId()));
            access.setUser(user);

            // Проверяем уникальность новой комбинации user + storage
            if (userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(
                    dto.getUserId(), dto.getStorageId(), access.getId())) {
                throw new DuplicateUserStorageAccessException(
                        "User storage access already exists for user ID: " + dto.getUserId() +
                                " and storage ID: " + dto.getStorageId());
            }
        }

        // Обновляем storage если изменился
        if (!access.getStorage().getId().equals(dto.getStorageId())) {
            Storage storage = storageRepository.findById(dto.getStorageId())
                    .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + dto.getStorageId()));
            access.setStorage(storage);

            // Проверяем уникальность новой комбинации user + storage
            if (userStorageAccessRepository.existsByUserIdAndStorageIdAndIdNot(
                    dto.getUserId(), dto.getStorageId(), access.getId())) {
                throw new DuplicateUserStorageAccessException(
                        "User storage access already exists for user ID: " + dto.getUserId() +
                                " and storage ID: " + dto.getStorageId());
            }
        }

        // Обновляем grantedBy если изменился
        if (!access.getGrantedBy().getId().equals(dto.getGrantedById())) {
            User grantedBy = userRepository.findById(dto.getGrantedById())
                    .orElseThrow(() -> new UserNotFoundException("Granted by user not found with ID: " + dto.getGrantedById()));
            access.setGrantedBy(grantedBy);
        }
    }

    // Дополнительные методы

    @Transactional(readOnly = true)
    public UserStorageAccessDTO findByUserAndStorage(Long userId, Long storageId) {
        log.debug("Finding user storage access by userId: {} and storageId: {}", userId, storageId);

        UserStorageAccess access = userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId)
                .orElseThrow(() -> new UserStorageAccessNotFoundException(
                        "User storage access not found for user ID: " + userId + " and storage ID: " + storageId));

        return userStorageAccessMapper.toDTO(access);
    }

    @Transactional(readOnly = true)
    public boolean hasAccess(Long userId, Long storageId, AccessLevel requiredLevel) {
        log.debug("Checking access for userId: {}, storageId: {}, requiredLevel: {}", userId, storageId, requiredLevel);

        UserStorageAccess access = userStorageAccessRepository.findByUserIdAndStorageId(userId, storageId)
                .orElse(null);

        if (access == null || !access.getIsActive()) {
            return false;
        }

        // Проверяем expiration date
        if (access.getExpiresAt() != null && access.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Проверяем уровень доступа
        return isAccessLevelSufficient(access.getAccessLevel(), requiredLevel);
    }

    @Transactional
    public UserStorageAccessDTO deactivate(Long id) {
        log.info("Deactivating user storage access with ID: {}", id);

        UserStorageAccess access = userStorageAccessRepository.findById(id)
                .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));

        access.setIsActive(false);
        UserStorageAccess updatedAccess = userStorageAccessRepository.save(access);

        log.info("User storage access with ID: {} deactivated successfully", id);
        return userStorageAccessMapper.toDTO(updatedAccess);
    }

    @Transactional
    public UserStorageAccessDTO activate(Long id) {
        log.info("Activating user storage access with ID: {}", id);

        UserStorageAccess access = userStorageAccessRepository.findById(id)
                .orElseThrow(() -> new UserStorageAccessNotFoundException("User storage access not found with ID: " + id));

        access.setIsActive(true);
        UserStorageAccess updatedAccess = userStorageAccessRepository.save(access);

        log.info("User storage access with ID: {} activated successfully", id);
        return userStorageAccessMapper.toDTO(updatedAccess);
    }

    @Transactional(readOnly = true)
    public List<UserStorageAccessDTO> findByUser(Long userId) {
        log.debug("Finding all user storage accesses for userId: {}", userId);

        List<UserStorageAccess> accesses = userStorageAccessRepository.findByUserId(userId);

        return accesses.stream()
                .map(userStorageAccessMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserStorageAccessDTO> findByStorage(Long storageId) {
        log.debug("Finding all user storage accesses for storageId: {}", storageId);

        List<UserStorageAccess> accesses = userStorageAccessRepository.findByStorageId(storageId);

        return accesses.stream()
                .map(userStorageAccessMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserStorageAccessDTO> findExpiredAccesses() {
        log.debug("Finding all expired user storage accesses");

        List<UserStorageAccess> expiredAccesses = userStorageAccessRepository.findExpiredAccesses(LocalDateTime.now());

        return expiredAccesses.stream()
                .map(userStorageAccessMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
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
        // BASIC < MANAGER < ADMIN
        return userLevel.ordinal() >= requiredLevel.ordinal();
    }

    @Transactional(readOnly = true)
    public long countActiveAccessesByUser(Long userId) {
        log.debug("Counting active accesses for userId: {}", userId);
        return userStorageAccessRepository.countByUserIdAndIsActive(userId, true);
    }

    @Transactional(readOnly = true)
    public long countActiveAccessesByStorage(Long storageId) {
        log.debug("Counting active accesses for storageId: {}", storageId);
        return userStorageAccessRepository.countByStorageIdAndIsActive(storageId, true);
    }
}
