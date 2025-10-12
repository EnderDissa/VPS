package com.example.warehouse.service;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.enumeration.MaintenanceStatus;
import org.springframework.data.domain.Page;

public interface ItemMaintenanceService {
    ItemMaintenanceDTO create(ItemMaintenanceDTO dto);
    ItemMaintenanceDTO getById(Long id);
    void update(Long id, ItemMaintenanceDTO dto);
    void delete(Long id);
    Page<ItemMaintenanceDTO> findPage(int page, int size, Long itemId, MaintenanceStatus status);
}
