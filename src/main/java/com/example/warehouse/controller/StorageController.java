package com.example.warehouse.controller;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.service.interfaces.StorageService;

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
@RequestMapping("/api/v1/storages")
@Tag(name = "Storages")
public class StorageController {

    private final StorageService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public StorageController(StorageService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create storage")
    public ResponseEntity<StorageDTO> create(@Valid @RequestBody StorageDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get storage by id")
    public StorageDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update storage by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody StorageDTO dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete storage by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List storages with pagination and total count")
    public ResponseEntity<List<StorageDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String nameLike
    ) {
        var result = service.findPage(page, size, nameLike);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }
}
