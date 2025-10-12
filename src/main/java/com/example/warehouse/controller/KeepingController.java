package com.example.warehouse.controller;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.service.interfaces.KeepingService;

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
@RequestMapping("/api/v1/keeping")
@Tag(name = "Keeping")
public class KeepingController {

    private final KeepingService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public KeepingController(KeepingService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create keeping link")
    public ResponseEntity<KeepingDTO> create(@Valid @RequestBody KeepingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get keeping by id")
    public KeepingDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update keeping by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody KeepingDTO dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete keeping by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List keeping with pagination and total count")
    public ResponseEntity<List<KeepingDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) Long storageId,
            @RequestParam(required = false) Long itemId
    ) {
        var result = service.findPage(page, size, storageId, itemId);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }
}
