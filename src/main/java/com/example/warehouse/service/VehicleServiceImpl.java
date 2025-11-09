package com.example.warehouse.service;

import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.repository.VehicleRepository;
import com.example.warehouse.service.interfaces.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Vehicle create(Vehicle vehicle) {
        log.info("Creating new vehicle with license plate: {}", vehicle.getLicensePlate());

        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new IllegalArgumentException("Vehicle with license plate '" + vehicle.getLicensePlate() + "' already exists");
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
        return savedVehicle;
    }

    @Override
    public Vehicle getById(Long id) {
        log.info("Getting vehicle by ID: {}", id);

        return (vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id)));
    }

    @Override
    public void update(Long id, Vehicle vehicle) {
        log.info("Updating vehicle with ID: {}", id);

        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));

        if (!existingVehicle.getLicensePlate().equals(vehicle.getLicensePlate()) &&
                vehicleRepository.existsByLicensePlateAndIdNot(vehicle.getLicensePlate(), id)) {
            throw new IllegalArgumentException("Vehicle with license plate '" + vehicle.getLicensePlate() + "' already exists");
        }

        existingVehicle.setBrand(vehicle.getBrand());
        existingVehicle.setModel(vehicle.getModel());
        existingVehicle.setLicensePlate(vehicle.getLicensePlate());
        existingVehicle.setYear(vehicle.getYear());
        existingVehicle.setCapacity(vehicle.getCapacity());
        existingVehicle.setStatus(vehicle.getStatus());

        vehicleRepository.save(existingVehicle);
        log.info("Vehicle updated successfully with ID: {}", id);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting vehicle with ID: {}", id);

        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with ID: " + id);
        }

        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted successfully with ID: {}", id);
    }

    @Override
    public Page<Vehicle> findPage(int page, int size, VehicleStatus status, String brand, String model) {
        log.info("Finding vehicles page - page: {}, size: {}, status: {}, brand: {}, model: {}",
                page, size, status, brand, model);

        Pageable pageable = PageRequest.of(page, size, Sort.by("brand").and(Sort.by("model")));

        Page<Vehicle> vehicles;

        if (status != null && brand != null && model != null) {
            vehicles = vehicleRepository.findByStatusAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                    status, brand, model, pageable);
        } else if (status != null && brand != null) {
            vehicles = vehicleRepository.findByStatusAndBrandContainingIgnoreCase(status, brand, pageable);
        } else if (status != null && model != null) {
            vehicles = vehicleRepository.findByStatusAndModelContainingIgnoreCase(status, model, pageable);
        } else if (brand != null && model != null) {
            vehicles = vehicleRepository.findByBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                    brand, model, pageable);
        } else if (status != null) {
            vehicles = vehicleRepository.findByStatus(status, pageable);
        } else if (brand != null) {
            vehicles = vehicleRepository.findByBrandContainingIgnoreCase(brand, pageable);
        } else if (model != null) {
            vehicles = vehicleRepository.findByModelContainingIgnoreCase(model, pageable);
        } else {
            vehicles = vehicleRepository.findAll(pageable);
        }

        return vehicles;
    }

    public List<Vehicle> findByStatus(VehicleStatus status) {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(status, Pageable.unpaged()).getContent();
        return vehicles.stream()
                .toList();
    }

    public Vehicle findByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with license plate: " + licensePlate));
    }

    public List<Vehicle> findAvailableVehicles() {
        return findByStatus(VehicleStatus.AVAILABLE);
    }
}