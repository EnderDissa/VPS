package com.example.warehouse.repository;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    Page<Vehicle> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    Page<Vehicle> findByModelContainingIgnoreCase(String model, Pageable pageable);

    Page<Vehicle> findByStatusAndBrandContainingIgnoreCase(VehicleStatus status, String brand, Pageable pageable);

    Page<Vehicle> findByStatusAndModelContainingIgnoreCase(VehicleStatus status, String model, Pageable pageable);

    Page<Vehicle> findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(String brand, String model, Pageable pageable);

    Page<Vehicle> findByStatusAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
            VehicleStatus status, String brand, String model, Pageable pageable);
}