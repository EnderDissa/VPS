package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;
import com.example.warehouse.enumeration.TransportStatus;
import org.springframework.data.domain.Page;

public interface TransportationService {
    Transportation create(Transportation transportation);
    Transportation getById(Long id);
    void update(Long id, Transportation transportation);
    void delete(Long id);
    Page<Transportation> findPage(int page, int size, TransportStatus status, Long itemId,
                                     Long fromStorageId, Long toStorageId);
}
