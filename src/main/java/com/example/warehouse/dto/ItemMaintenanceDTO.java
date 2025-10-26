package com.example.warehouse.dto;

import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.enumeration.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

public record ItemMaintenanceDTO(
        Long id,

        @NotNull(message = "Item ID is required")
        Long itemId,

        @NotNull(message = "Technician ID is required")
        Long technicianId,

        @NotNull(message = "Maintenance date is required")
        LocalDateTime maintenanceDate,

        LocalDateTime nextMaintenanceDate,

        @PositiveOrZero(message = "Cost must be positive or zero")
        BigDecimal cost,

        String description,

        @NotNull(message = "Status is required")
        MaintenanceStatus status,

        LocalDateTime createdAt
) {

    public ItemMaintenanceDTO(ItemMaintenance maintenance) {
        this(
                maintenance != null ? maintenance.getId() : null,
                maintenance != null && maintenance.getItem() != null ? maintenance.getItem().getId() : null,
                maintenance != null && maintenance.getTechnician() != null ? maintenance.getTechnician().getId() : null,
                maintenance != null ? maintenance.getMaintenanceDate() : null,
                maintenance != null ? maintenance.getNextMaintenanceDate() : null,
                maintenance != null ? maintenance.getCost() : null,
                maintenance != null ? maintenance.getDescription() : null,
                maintenance != null ? maintenance.getStatus() : null,
                maintenance != null ? maintenance.getCreatedAt() : null
        );
    }
}
