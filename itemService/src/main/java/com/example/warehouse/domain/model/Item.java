package com.example.warehouse.domain.model;

import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.domain.enumeration.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private Long id;
    private String name;
    private ItemType type;
    private ItemCondition condition;
    private String serialNumber;
    private String description;
    private LocalDateTime createdAt;

    public boolean canBeBorrowed() {
        return condition != ItemCondition.UNDER_REPAIR
                && condition != ItemCondition.DECOMMISSIONED
                && condition != ItemCondition.NEEDS_MAINTENANCE;
    }


}
