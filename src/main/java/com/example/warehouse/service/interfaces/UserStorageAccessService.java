package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import org.springframework.data.domain.Page;

public interface UserStorageAccessService {
    UserStorageAccess create(UserStorageAccess userStorageAccess);
    UserStorageAccess getById(Long id);
    void update(Long id, UserStorageAccess userStorageAccess);
    void delete(Long id);
    Page<UserStorageAccess> findPage(int page, int size, Long userId, Long storageId, AccessLevel accessLevel, Boolean active);
}
