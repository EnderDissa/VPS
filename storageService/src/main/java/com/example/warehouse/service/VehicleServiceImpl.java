package com.example.warehouse.service;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.repository.VehicleRepository;
import com.example.warehouse.service.interfaces.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Mono<Vehicle> create(Vehicle vehicle) {
        log.info("Creating new vehicle with license plate: {}", vehicle.getLicensePlate());

        return vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Vehicle with license plate '" + vehicle.getLicensePlate() + "' already exists"));
                    }
                    return vehicleRepository.save(vehicle);
                })
                .doOnSuccess(saved -> log.info("Vehicle created successfully with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to create vehicle: {}", error.getMessage()));
    }

    @Override
    public Mono<Vehicle> getById(Long id) {
        log.info("Getting vehicle by ID: {}", id);

        return vehicleRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new IllegalArgumentException("Vehicle not found with ID: " + id)))
                .doOnSuccess(vehicle -> log.debug("Successfully fetched vehicle: {}", vehicle.getLicensePlate()))
                .doOnError(error -> log.error("Failed to fetch vehicle with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Vehicle> update(Long id, Vehicle vehicle) {
        log.info("Updating vehicle with ID: {}", id);

        return vehicleRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new IllegalArgumentException("Vehicle not found with ID: " + id)))
                .flatMap(existingVehicle -> {
                    // Проверяем уникальность license plate
                    if (!existingVehicle.getLicensePlate().equals(vehicle.getLicensePlate())) {
                        return vehicleRepository.existsByLicensePlateAndIdNot(vehicle.getLicensePlate(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new IllegalArgumentException(
                                                "Vehicle with license plate '" + vehicle.getLicensePlate() + "' already exists"));
                                    }
                                    return updateVehicleFields(existingVehicle, vehicle);
                                });
                    }
                    return updateVehicleFields(existingVehicle, vehicle);
                })
                .doOnSuccess(updated -> log.info("Vehicle updated successfully with ID: {}", id))
                .doOnError(error -> log.error("Failed to update vehicle with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting vehicle with ID: {}", id);

        return vehicleRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Vehicle not found with ID: " + id));
                    }
                    return vehicleRepository.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Vehicle deleted successfully with ID: {}", id))
                .doOnError(error -> log.error("Failed to delete vehicle with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Page<Vehicle>> findPage(int page, int size, VehicleStatus status, String brand, String model) {
        log.info("Finding vehicles page - page: {}, size: {}, status: {}, brand: {}, model: {}",
                page, size, status, brand, model);

        Pageable pageable = PageRequest.of(page, size, Sort.by("brand").and(Sort.by("model")));

        Flux<Vehicle> dataFlux = createDataFlux(status, brand, model, pageable);
        Mono<Long> totalMono = createTotalMono(status, brand, model);

        return dataFlux.collectList()
                .zipWith(totalMono)
                .map(tuple -> {
                    List<Vehicle> content = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    return (Page<Vehicle>) new PageImpl<>(content, pageable, totalElements);
                })
                .doOnSuccess(pageResult ->
                        log.debug("Fetched {} vehicles", pageResult.getNumberOfElements()))
                .doOnError(error ->
                        log.error("Failed to fetch vehicles page: {}", error.getMessage()));
    }

    @Override
    public Flux<Vehicle> findByStatus(VehicleStatus status) {
        log.debug("Finding vehicles by status: {}", status);

        return vehicleRepository.findByStatus(status)
                .doOnComplete(() -> log.debug("Completed fetching vehicles by status: {}", status))
                .doOnError(error -> log.error("Failed to fetch vehicles by status: {}", error.getMessage()));
    }

    @Override
    public Mono<Vehicle> findByLicensePlate(String licensePlate) {
        log.debug("Finding vehicle by license plate: {}", licensePlate);

        return vehicleRepository.findByLicensePlate(licensePlate)
                .switchIfEmpty(Mono.error(() ->
                        new IllegalArgumentException("Vehicle not found with license plate: " + licensePlate)))
                .doOnSuccess(vehicle -> log.debug("Found vehicle: {}", vehicle.getLicensePlate()))
                .doOnError(error -> log.error("Failed to fetch vehicle by license plate: {}", error.getMessage()));
    }

    @Override
    public Flux<Vehicle> findAvailableVehicles() {
        log.debug("Finding available vehicles");

        return findByStatus(VehicleStatus.AVAILABLE)
                .doOnComplete(() -> log.debug("Completed fetching available vehicles"))
                .doOnError(error -> log.error("Failed to fetch available vehicles: {}", error.getMessage()));
    }

    // Вспомогательные методы
    private Mono<Vehicle> updateVehicleFields(Vehicle existing, Vehicle updated) {
        existing.setBrand(updated.getBrand());
        existing.setModel(updated.getModel());
        existing.setLicensePlate(updated.getLicensePlate());
        existing.setYear(updated.getYear());
        existing.setCapacity(updated.getCapacity());
        existing.setStatus(updated.getStatus());

        return vehicleRepository.save(existing);
    }

    private Flux<Vehicle> createDataFlux(VehicleStatus status, String brand, String model, Pageable pageable) {
        if (status != null && brand != null && model != null) {
            return vehicleRepository.findByStatusAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                    status, brand, model, pageable);
        } else if (status != null && brand != null) {
            return vehicleRepository.findByStatusAndBrandContainingIgnoreCase(status, brand, pageable);
        } else if (status != null && model != null) {
            return vehicleRepository.findByStatusAndModelContainingIgnoreCase(status, model, pageable);
        } else if (brand != null && model != null) {
            return vehicleRepository.findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                    brand, model, pageable);
        } else if (status != null) {
            return vehicleRepository.findByStatus(status, pageable);
        } else if (brand != null) {
            return vehicleRepository.findByBrandContainingIgnoreCase(brand, pageable);
        } else if (model != null) {
            return vehicleRepository.findByModelContainingIgnoreCase(model, pageable);
        } else {
            return vehicleRepository.findAllBy(pageable);
        }
    }

    private Mono<Long> createTotalMono(VehicleStatus status, String brand, String model) {
        if (status != null && brand != null && model != null) {
            return vehicleRepository.findByStatusAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                    status, brand, model, Pageable.unpaged()).count();
        } else if (status != null && brand != null) {
            return vehicleRepository.findByStatusAndBrandContainingIgnoreCase(status, brand, Pageable.unpaged()).count();
        } else if (status != null && model != null) {
            return vehicleRepository.findByStatusAndModelContainingIgnoreCase(status, model, Pageable.unpaged()).count();
        } else if (brand != null && model != null) {
            return vehicleRepository.findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                    brand, model, Pageable.unpaged()).count();
        } else if (status != null) {
            return vehicleRepository.findByStatus(status, Pageable.unpaged()).count();
        } else if (brand != null) {
            return vehicleRepository.findByBrandContainingIgnoreCase(brand, Pageable.unpaged()).count();
        } else if (model != null) {
            return vehicleRepository.findByModelContainingIgnoreCase(model, Pageable.unpaged()).count();
        } else {
            return vehicleRepository.count();
        }
    }
}