package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.StorageDTO;
import org.springframework.data.domain.Page;

public interface StorageService {
    StorageDTO create(StorageDTO dto);
    StorageDTO getById(Long id);
    void update(Long id, StorageDTO dto);
    void delete(Long id);
    Page<StorageDTO> findPage(int page, int size, String nameLike);
}
