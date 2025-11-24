package com.example.warehouse.service;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.exception.DuplicateLicensePlateException;
import com.example.warehouse.exception.VehicleNotFoundException;
import com.example.warehouse.repository.VehicleRepository;
import com.example.warehouse.service.interfaces.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    // === CREATE ===
    @Override
    public Mono<Vehicle> create(Vehicle vehicle) {
        log.info("Creating new vehicle with license plate: {}", vehicle.getLicensePlate());

        return Mono.fromCallable(() -> {
                    if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
                        log.warn("Vehicle with license plate '{}' already exists", vehicle.getLicensePlate());
                        throw new DuplicateLicensePlateException(
                                "Vehicle with license plate '" + vehicle.getLicensePlate() + "' already exists"
                        );
                    }
                    return vehicleRepository.save(vehicle);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(saved -> log.info("Vehicle created successfully with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to create vehicle: {}", error.getMessage()));
    }

    // === GET BY ID ===
    @Override
    public Mono<Vehicle> getById(Long id) {
        return Mono.fromCallable(() ->
                        vehicleRepository.findById(id)
                                .orElseThrow(() -> {
                                    log.warn("Vehicle not found with ID: {}", id);
                                    return new VehicleNotFoundException("Vehicle not found with ID: " + id);
                                })
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(vehicle -> log.debug("Successfully fetched vehicle: {}", vehicle.getLicensePlate()))
                .doOnError(error -> log.error("Failed to fetch vehicle with ID {}: {}", id, error.getMessage()));
    }

    // === UPDATE ===
    @Override
    public Mono<Vehicle> update(Long id, Vehicle updatedVehicle) {
        log.info("Updating vehicle with ID: {}", id);

        return Mono.fromCallable(() -> {
                    Vehicle existing = vehicleRepository.findById(id)
                            .orElseThrow(() -> {
                                log.warn("Vehicle not found with ID: {}", id);
                                return new VehicleNotFoundException("Vehicle not found with ID: " + id);
                            });

                    if (!existing.getLicensePlate().equals(updatedVehicle.getLicensePlate())) {
                        if (vehicleRepository.existsByLicensePlateAndIdNot(updatedVehicle.getLicensePlate(), id)) {
                            log.warn("Vehicle with license plate '{}' already exists", updatedVehicle.getLicensePlate());
                            throw new DuplicateLicensePlateException(
                                    "Vehicle with license plate '" + updatedVehicle.getLicensePlate() + "' already exists"
                            );
                        }
                    }

                    existing.setBrand(updatedVehicle.getBrand());
                    existing.setModel(updatedVehicle.getModel());
                    existing.setLicensePlate(updatedVehicle.getLicensePlate());
                    existing.setYear(updatedVehicle.getYear());
                    existing.setCapacity(updatedVehicle.getCapacity());
                    existing.setStatus(updatedVehicle.getStatus());

                    return vehicleRepository.save(existing);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(updated -> log.info("Vehicle updated successfully with ID: {}", id))
                .doOnError(error -> log.error("Failed to update vehicle with ID {}: {}", id, error.getMessage()));
    }

    // === DELETE ===
    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> {
                    if (!vehicleRepository.existsById(id)) {
                        log.warn("Vehicle not found with ID: {}", id);
                        throw new VehicleNotFoundException("Vehicle not found with ID: " + id);
                    }
                    vehicleRepository.deleteById(id);
                    log.info("Vehicle deleted successfully with ID: {}", id);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .doOnError(error -> log.error("Failed to delete vehicle with ID {}: {}", id, error.getMessage()));
    }

    // === PAGE WITH FILTERS ===
    @Override
    public Mono<Page<Vehicle>> findPage(int page, int size, VehicleStatus status, String brand, String model) {
        log.debug("Finding vehicles page - page: {}, size: {}, status: {}, brand: {}, model: {}",
                page, size, status, brand, model);

        return Mono.fromCallable(() -> {
                    PageRequest pageable = PageRequest.of(page, size, Sort.by("brand").and(Sort.by("model")));

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
                        return vehicleRepository.findAll(pageable);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(pageResult -> log.debug("Fetched {} vehicles", pageResult.getNumberOfElements()))
                .doOnError(error -> log.error("Failed to fetch vehicles page: {}", error.getMessage()));
    }

    @Override
    public Flux<Vehicle> findByStatus(VehicleStatus status) {
        return Mono.fromCallable(() ->
                        vehicleRepository.findByStatus(status, Pageable.unpaged())  // ← Pageable.unpaged()
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    // === FIND BY LICENSE PLATE ===
    @Override
    public Mono<Vehicle> findByLicensePlate(String licensePlate) {
        return Mono.fromCallable(() ->
                        vehicleRepository.findByLicensePlate(licensePlate)
                                .orElseThrow(() -> {
                                    log.warn("Vehicle not found with license plate: {}", licensePlate);
                                    return new VehicleNotFoundException("Vehicle not found with license plate: " + licensePlate);
                                })
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(vehicle -> log.debug("Found vehicle: {}", vehicle.getLicensePlate()))
                .doOnError(error -> log.error("Failed to fetch vehicle by license plate: {}", error.getMessage()));
    }

    // === FIND AVAILABLE VEHICLES ===
    @Override
    public Flux<Vehicle> findAvailableVehicles() {
        log.debug("Finding available vehicles (status = {})", VehicleStatus.AVAILABLE);
        return findByStatus(VehicleStatus.AVAILABLE);
    }
}