package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import org.springframework.data.domain.Page;

public interface VehicleService {
    Vehicle create(Vehicle vehicle);
    Vehicle getById(Long id);
    void update(Long id, Vehicle Vehicle);
    void delete(Long id);
    Page<Vehicle> findPage(int page, int size, VehicleStatus status, String brand, String model);
}
