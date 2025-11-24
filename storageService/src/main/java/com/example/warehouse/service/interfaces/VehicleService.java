package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VehicleService {
    Mono<Vehicle> create(Vehicle vehicle);
    Mono<Vehicle> getById(Long id);
    Mono<Vehicle> update(Long id, Vehicle vehicle);
    Mono<Void> delete(Long id);
    Mono<Page<Vehicle>> findPage(int page, int size, VehicleStatus status, String brand, String model);

    Flux<Vehicle> findByStatus(VehicleStatus status);
    Mono<Vehicle> findByLicensePlate(String licensePlate);
    Flux<Vehicle> findAvailableVehicles();
}
