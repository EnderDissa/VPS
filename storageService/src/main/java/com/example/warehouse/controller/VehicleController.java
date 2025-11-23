package com.example.warehouse.controller;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.mapper.VehicleMapper;
import com.example.warehouse.service.interfaces.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;
    private final VehicleMapper mapper;

    @PostMapping
    @Operation(summary = "Create vehicle")
    public Mono<ResponseEntity<VehicleDTO>> create(@Valid @RequestBody VehicleDTO dto) {
        return service.create(mapper.toEntity(dto))
                .map(vehicle -> ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(vehicle)))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().build()))
                .doOnSuccess(response -> log.debug("Vehicle created successfully"))
                .doOnError(error -> log.error("Error creating vehicle: {}", error.getMessage()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by id")
    public Mono<ResponseEntity<VehicleDTO>> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(vehicle -> ResponseEntity.ok(mapper.toDTO(vehicle)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.notFound().build()))
                .doOnSuccess(response -> {
                    if (response.getStatusCode() == HttpStatus.OK) {
                        log.debug("Vehicle found with ID: {}", id);
                    }
                })
                .doOnError(error -> log.error("Error fetching vehicle with ID {}: {}", id, error.getMessage()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle by id")
    public Mono<ResponseEntity<Void>> update(@PathVariable Long id, @Valid @RequestBody VehicleDTO dto) {
        return service.update(id, mapper.toEntity(dto))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.notFound().build()))
                .doOnSuccess(response -> log.debug("Vehicle updated with ID: {}", id))
                .doOnError(error -> log.error("Error updating vehicle with ID {}: {}", id, error.getMessage()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle by id")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.notFound().build()))
                .doOnSuccess(response -> log.debug("Vehicle deleted with ID: {}", id))
                .doOnError(error -> log.error("Error deleting vehicle with ID {}: {}", id, error.getMessage()));
    }

    @GetMapping
    @Operation(summary = "List vehicles with pagination and total count")
    public Mono<ResponseEntity<List<VehicleDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model
    ) {
        return service.findPage(page, size, status, brand, model)
                .map(pageResult -> {
                    List<VehicleDTO> content = pageResult.getContent().stream()
                            .map(mapper::toDTO)
                            .collect(Collectors.toList());

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(pageResult.getTotalElements()));
                    headers.add("X-Total-Pages", String.valueOf(pageResult.getTotalPages()));
                    headers.add("X-Current-Page", String.valueOf(page));
                    headers.add("X-Page-Size", String.valueOf(size));

                    return new ResponseEntity<>(content, headers, HttpStatus.OK);
                })
                .doOnSuccess(response -> log.debug("Fetched vehicles page: {}, size: {}", page, size))
                .doOnError(error -> log.error("Error fetching vehicles page: {}", error.getMessage()));
    }

    // Дополнительные endpoints для реактивных операций
    @GetMapping("/status/{status}")
    @Operation(summary = "Get vehicles by status")
    public Flux<VehicleDTO> getByStatus(@PathVariable VehicleStatus status) {
        return service.findByStatus(status)
                .map(mapper::toDTO)
                .doOnComplete(() -> log.debug("Completed streaming vehicles by status: {}", status))
                .doOnError(error -> log.error("Error streaming vehicles by status: {}", error.getMessage()));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available vehicles")
    public Flux<VehicleDTO> getAvailableVehicles() {
        return service.findAvailableVehicles()
                .map(mapper::toDTO)
                .doOnComplete(() -> log.debug("Completed streaming available vehicles"))
                .doOnError(error -> log.error("Error streaming available vehicles: {}", error.getMessage()));
    }

    @GetMapping("/license-plate/{licensePlate}")
    @Operation(summary = "Get vehicle by license plate")
    public Mono<ResponseEntity<VehicleDTO>> getByLicensePlate(@PathVariable String licensePlate) {
        return service.findByLicensePlate(licensePlate)
                .map(vehicle -> ResponseEntity.ok(mapper.toDTO(vehicle)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnSuccess(response -> {
                    if (response.getStatusCode() == HttpStatus.OK) {
                        log.debug("Vehicle found with license plate: {}", licensePlate);
                    }
                })
                .doOnError(error -> log.error("Error fetching vehicle with license plate {}: {}", licensePlate, error.getMessage()));
    }
}