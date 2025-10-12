package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.enumeration.AccessLevel;
import org.springframework.data.domain.Page;

public interface UserStorageAccessService {
    UserStorageAccessDTO create(UserStorageAccessDTO dto);
    UserStorageAccessDTO getById(Long id);
    void update(Long id, UserStorageAccessDTO dto);
    void delete(Long id);
    Page<UserStorageAccessDTO> findPage(int page, int size, Long userId, Long storageId, AccessLevel accessLevel, Boolean active);
}
