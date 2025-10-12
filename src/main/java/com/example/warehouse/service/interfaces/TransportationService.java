package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.enumeration.TransportStatus;
import org.springframework.data.domain.Page;

public interface TransportationService {
    TransportationDTO create(TransportationDTO dto);
    TransportationDTO getById(Long id);
    void update(Long id, TransportationDTO dto);
    void delete(Long id);
    Page<TransportationDTO> findPage(int page, int size, TransportStatus status, Long itemId,
                                     Long fromStorageId, Long toStorageId);
}
