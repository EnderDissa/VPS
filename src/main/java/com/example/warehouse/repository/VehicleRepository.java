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

    // Поиск по статусу
    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    // Поиск по бренду
    Page<Vehicle> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    // Поиск по модели
    Page<Vehicle> findByModelContainingIgnoreCase(String model, Pageable pageable);

    // Поиск по номерному знаку
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    // Комбинированный поиск по статусу и бренду
    Page<Vehicle> findByStatusAndBrandContainingIgnoreCase(VehicleStatus status, String brand, Pageable pageable);

    // Комбинированный поиск по статусу и модели
    Page<Vehicle> findByStatusAndModelContainingIgnoreCase(VehicleStatus status, String model, Pageable pageable);

    // Универсальный поиск с фильтрами
    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:status IS NULL OR v.status = :status) AND " +
            "(:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%')))")
    Page<Vehicle> findByFilters(@Param("status") VehicleStatus status,
                                @Param("brand") String brand,
                                @Param("model") String model,
                                Pageable pageable);

    // Проверка существования по номерному знаку (для валидации)
    boolean existsByLicensePlate(String licensePlate);

    // Проверка существования по номерному знаку исключая текущий ID (для обновления)
    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);
}