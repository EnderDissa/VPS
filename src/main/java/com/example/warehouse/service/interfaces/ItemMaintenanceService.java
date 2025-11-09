package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.enumeration.MaintenanceStatus;
import org.springframework.data.domain.Page;

public interface ItemMaintenanceService {
    ItemMaintenance create(ItemMaintenance itemMaintenance);
    ItemMaintenance getById(Long id);
    void update(Long id, ItemMaintenance itemMaintenance);
    void delete(Long id);
    Page<ItemMaintenance> findPage(int page, int size, Long itemId, MaintenanceStatus status);
}
