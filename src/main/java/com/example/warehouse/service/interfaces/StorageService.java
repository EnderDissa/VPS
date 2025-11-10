package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.entity.Storage;
import org.springframework.data.domain.Page;

public interface StorageService {
    Storage create(Storage storage);
    Storage getById(Long id);
    void update(Long id, Storage storage);
    void delete(Long id);
    Page<Storage> findPage(int page, int size, String nameLike);
}
