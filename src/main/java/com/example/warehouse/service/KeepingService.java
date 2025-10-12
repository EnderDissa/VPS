package com.example.warehouse.service;

import com.example.warehouse.dto.KeepingDTO;
import org.springframework.data.domain.Page;

public interface KeepingService {
    KeepingDTO create(KeepingDTO dto);
    KeepingDTO getById(Long id);
    void update(Long id, KeepingDTO dto);
    void delete(Long id);
    Page<KeepingDTO> findPage(int page, int size, Long storageId, Long itemId);
}
