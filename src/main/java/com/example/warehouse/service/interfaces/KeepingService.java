package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;
import org.springframework.data.domain.Page;

public interface KeepingService {
    Keeping create(Keeping keeping);
    Keeping getById(Long id);
    void update(Long id, Keeping keeping);
    void delete(Long id);
    Page<Keeping> findPage(int page, int size, Long storageId, Long itemId);
}
