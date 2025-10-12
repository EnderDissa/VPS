package com.example.warehouse.controller;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.enumeration.MaintenanceStatus;
import com.example.warehouse.service.interfaces.ItemMaintenanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance")
public class ItemMaintenanceController {

    private final ItemMaintenanceService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ItemMaintenanceController(ItemMaintenanceService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create maintenance record")
    public ResponseEntity<ItemMaintenanceDTO> create(@Valid @RequestBody ItemMaintenanceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get maintenance by id")
    public ItemMaintenanceDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update maintenance by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ItemMaintenanceDTO dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete maintenance by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List maintenance with pagination and total count")
    public ResponseEntity<List<ItemMaintenanceDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) MaintenanceStatus status
    ) {
        var result = service.findPage(page, size, itemId, status);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }
}
