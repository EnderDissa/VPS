package com.example.warehouse.repository;

import com.example.warehouse.entity.Storage;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface VehicleRepository extends ReactiveCrudRepository<Vehicle, Long> {

    Flux<Vehicle> findAllBy(Pageable pageable);

    Flux<Vehicle> findByStatus(VehicleStatus status);

    Flux<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    Mono<Vehicle> findByLicensePlate(String licensePlate);

    Mono<Boolean> existsByLicensePlate(String licensePlate);

    Mono<Boolean> existsByLicensePlateAndIdNot(String licensePlate, Long id);

    Flux<Vehicle> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    Flux<Vehicle> findByModelContainingIgnoreCase(String model, Pageable pageable);

    Flux<Vehicle> findByStatusAndBrandContainingIgnoreCase(VehicleStatus status, String brand, Pageable pageable);

    Flux<Vehicle> findByStatusAndModelContainingIgnoreCase(VehicleStatus status, String model, Pageable pageable);

    Flux<Vehicle> findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(String brand, String model, Pageable pageable);

    Flux<Vehicle> findByStatusAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
            VehicleStatus status, String brand, String model, Pageable pageable);
}