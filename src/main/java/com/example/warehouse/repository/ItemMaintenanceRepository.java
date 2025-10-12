package com.example.warehouse.repository;

import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.enumeration.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemMaintenanceRepository extends JpaRepository<ItemMaintenance, Long> {

    Page<ItemMaintenance> findByItemId(Long itemId, Pageable pageable);

    Page<ItemMaintenance> findByStatus(MaintenanceStatus status, Pageable pageable);

    Page<ItemMaintenance> findByItemIdAndStatus(Long itemId, MaintenanceStatus status, Pageable pageable);

    Page<ItemMaintenance> findByTechnicianId(Long technicianId, Pageable pageable);

    long countByStatus(MaintenanceStatus status);

    List<ItemMaintenance> findByNextMaintenanceDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT im FROM ItemMaintenance im WHERE im.nextMaintenanceDate <= :date AND im.status = :status")
    List<ItemMaintenance> findUpcomingMaintenance(@Param("date") LocalDateTime date,
                                                  @Param("status") MaintenanceStatus status);

    @Query("SELECT COUNT(im) FROM ItemMaintenance im WHERE im.item.id = :itemId")
    long countByItemId(@Param("itemId") Long itemId);
}
