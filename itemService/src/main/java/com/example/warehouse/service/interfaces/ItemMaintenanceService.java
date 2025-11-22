package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.enumeration.MaintenanceStatus;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemMaintenanceService {
    Mono<ItemMaintenance> create(ItemMaintenance maintenance);
    Mono<ItemMaintenance> getById(Long id);
    Mono<Void> update(Long id, ItemMaintenance maintenance);
    Mono<Void> delete(Long id);
    Flux<ItemMaintenance> findMaintenancesByFilters(Long itemId, MaintenanceStatus status, Pageable pageable);
    Mono<Long> countMaintenancesByFilters(Long itemId, MaintenanceStatus status);
    Flux<ItemMaintenance> findByTechnician(Long technicianId, Pageable pageable);
    Mono<Long> countByStatus(MaintenanceStatus status);
    Mono<Void> updateStatus(Long id, MaintenanceStatus status);
}