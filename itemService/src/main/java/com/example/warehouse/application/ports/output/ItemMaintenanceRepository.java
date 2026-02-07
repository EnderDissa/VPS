package com.example.warehouse.application.ports.output;

import com.example.warehouse.infrastructure.persistence.entity.ItemMaintenance;
import com.example.warehouse.domain.enumeration.MaintenanceStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemMaintenanceRepository extends JpaRepository<ItemMaintenance, Long> {

    Page<ItemMaintenance> findByItemId(Long itemId, Pageable pageable);

    Page<ItemMaintenance> findByStatus(MaintenanceStatus status, Pageable pageable);

    Page<ItemMaintenance> findByItemIdAndStatus(Long itemId, MaintenanceStatus status, Pageable pageable);

    Page<ItemMaintenance> findByTechnicianId(Long technicianId, Pageable pageable);

    long countByItemId(Long itemId);

    long countByStatus(MaintenanceStatus status);

    long countByItemIdAndStatus(Long itemId, MaintenanceStatus status);

}
