package com.example.warehouse.domain.model;

import com.example.warehouse.domain.enumeration.TransportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transportation {

    private Long id;
    private Long itemId;
    private Vehicle vehicle;
    private Long driverId;
    private Storage fromStorage;
    private Storage toStorage;
    private TransportStatus status = TransportStatus.PLANNED;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime actualDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualArrival;
    private LocalDateTime createdAt;
}
