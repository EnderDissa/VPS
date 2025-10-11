package com.example.warehouse.dto;

import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.enumeration.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ItemMaintenanceDTO {

    public ItemMaintenanceDTO(ItemMaintenance maintenance) {
        if (maintenance == null) return;
        this.id = maintenance.getId();
        this.itemId = maintenance.getItem() != null ? maintenance.getItem().getId() : null;
        this.technicianId = maintenance.getTechnician() != null ? maintenance.getTechnician().getId() : null;
        this.maintenanceDate = maintenance.getMaintenanceDate();
        this.nextMaintenanceDate = maintenance.getNextMaintenanceDate();
        this.cost = maintenance.getCost();
        this.description = maintenance.getDescription();
        this.status = maintenance.getStatus();
        this.createdAt = maintenance.getCreatedAt();
    }

    private Long id;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotNull(message = "Maintenance date is required")
    private LocalDateTime maintenanceDate;

    private LocalDateTime nextMaintenanceDate;

    @PositiveOrZero(message = "Cost must be positive or zero")
    private BigDecimal cost;

    private String description;

    @NotNull(message = "Status is required")
    private MaintenanceStatus status = MaintenanceStatus.COMPLETED;

    private LocalDateTime createdAt;
}
