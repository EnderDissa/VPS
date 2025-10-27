package com.example.warehouse.service;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.mapper.VehicleMapper;
import com.example.warehouse.repository.VehicleRepository;
import com.example.warehouse.service.interfaces.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    @Transactional
    public VehicleDTO create(VehicleDTO dto) {
        log.info("Creating new vehicle with license plate: {}", dto.licensePlate());

        // Проверка уникальности номерного знака
        if (vehicleRepository.existsByLicensePlate(dto.licensePlate())) {
            throw new IllegalArgumentException("Vehicle with license plate '" + dto.licensePlate() + "' already exists");
        }

        Vehicle vehicle = vehicleMapper.toEntity(dto);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
        return vehicleMapper.toDTO(savedVehicle);
    }

    @Override
    public VehicleDTO getById(Long id) {
        log.info("Getting vehicle by ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));

        return vehicleMapper.toDTO(vehicle);
    }

    @Override
    @Transactional
    public void update(Long id, VehicleDTO dto) {
        log.info("Updating vehicle with ID: {}", id);

        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));

        // Проверка уникальности номерного знака при обновлении
        if (!existingVehicle.getLicensePlate().equals(dto.licensePlate()) &&
                vehicleRepository.existsByLicensePlateAndIdNot(dto.licensePlate(), id)) {
            throw new IllegalArgumentException("Vehicle with license plate '" + dto.licensePlate() + "' already exists");
        }

        // Обновление полей
        existingVehicle.setBrand(dto.brand());
        existingVehicle.setModel(dto.model());
        existingVehicle.setLicensePlate(dto.licensePlate());
        existingVehicle.setYear(dto.year());
        existingVehicle.setCapacity(dto.capacity());
        existingVehicle.setStatus(dto.status());

        vehicleRepository.save(existingVehicle);
        log.info("Vehicle updated successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting vehicle with ID: {}", id);

        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with ID: " + id);
        }

        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted successfully with ID: {}", id);
    }

    @Override
    public Page<VehicleDTO> findPage(int page, int size, VehicleStatus status, String brand, String model) {
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

        return vehicles.map(vehicleMapper::toDTO);
    }

    // Дополнительные методы для удобства

    public List<VehicleDTO> findByStatus(VehicleStatus status) {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(status, Pageable.unpaged()).getContent();
        return vehicles.stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }

    public VehicleDTO findByLicensePlate(String licensePlate) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with license plate: " + licensePlate));
        return vehicleMapper.toDTO(vehicle);
    }

    public List<VehicleDTO> findAvailableVehicles() {
        return findByStatus(VehicleStatus.AVAILABLE);
    }
}