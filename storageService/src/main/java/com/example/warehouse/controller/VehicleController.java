package com.example.warehouse.controller;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.enumeration.VehicleStatus;
import com.example.warehouse.mapper.VehicleMapper;
import com.example.warehouse.service.interfaces.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles")
@ConditionalOnBean(VehicleService.class)
public class VehicleController {

    private final VehicleService service;
    private final VehicleMapper mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public VehicleController(VehicleService service, VehicleMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Create vehicle")
    public ResponseEntity<VehicleDTO> create(@Valid @RequestBody VehicleDTO dto) {
        Vehicle vehicle = service.create(mapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(vehicle));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by id")
    public VehicleDTO getById(@PathVariable Long id) {
        return mapper.toDTO(service.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody VehicleDTO dto) {
        service.update(id, mapper.toEntity(dto));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List vehicles with pagination and total count")
    public ResponseEntity<List<VehicleDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model
    ) {
        var result = service.findPage(page, size, status, brand, model).map(mapper::toDTO);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }
}
