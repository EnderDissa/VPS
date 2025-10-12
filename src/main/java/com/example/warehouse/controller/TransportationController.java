package com.example.warehouse.controller;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.enumeration.TransportStatus;
import com.example.warehouse.service.TransportationService;
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
@RequestMapping("/api/v1/transportations")
@Tag(name = "Transportations")
public class TransportationController {

    private final TransportationService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TransportationController(TransportationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create transportation")
    public ResponseEntity<TransportationDTO> create(@Valid @RequestBody TransportationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transportation by id")
    public TransportationDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transportation by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody TransportationDTO dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transportation by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List transportations with pagination and total count")
    public ResponseEntity<List<TransportationDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) TransportStatus status,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long fromStorageId,
            @RequestParam(required = false) Long toStorageId
    ) {
        var result = service.findPage(page, size, status, itemId, fromStorageId, toStorageId);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }
}
