package com.example.warehouse.domain.model;

import com.example.warehouse.domain.enumeration.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemMaintenance {

    private Long id;
    private Item item;
    private Long technicianId;
    private LocalDateTime maintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private BigDecimal cost;
    private String description;
    private MaintenanceStatus status = MaintenanceStatus.COMPLETED;
    private LocalDateTime createdAt;
}
