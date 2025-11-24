package com.example.warehouse.controller;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;
import com.example.warehouse.enumeration.TransportStatus;
import com.example.warehouse.mapper.TransportationMapper;
import com.example.warehouse.service.interfaces.TransportationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/transportations")
@Tag(name = "Transportations", description = "Transportation management API")
@RequiredArgsConstructor
public class TransportationController {

    private final TransportationService service;
    private final TransportationMapper mapper;

    @PostMapping
    @Operation(summary = "Create a new transportation")
    public Mono<ResponseEntity<TransportationDTO>> create(@Valid @RequestBody TransportationDTO dto) {
        return service.create(mapper.toEntity(dto))
                .map(mapper::toDTO)
                .map(dtoResult -> ResponseEntity.status(HttpStatus.CREATED).body(dtoResult));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transportation by ID")
    public Mono<TransportationDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(mapper::toDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transportation by ID")
    public Mono<ResponseEntity<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody TransportationDTO dto) {
        return service.update(id, mapper.toEntity(dto))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transportation by ID")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping
    @Operation(summary = "List transportations with pagination and filtering")
    public Mono<ResponseEntity<List<TransportationDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size,
            @RequestParam(required = false) TransportStatus status,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long fromStorageId,
            @RequestParam(required = false) Long toStorageId) {

        return service.findPage(page, size, status, itemId, fromStorageId, toStorageId)
                .map(pageResult -> {
                    List<TransportationDTO> dtos = pageResult.getContent().stream()
                            .map(mapper::toDTO)
                            .toList();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(pageResult.getTotalElements()));
                    headers.add("X-Page-Number", String.valueOf(pageResult.getNumber()));
                    headers.add("X-Page-Size", String.valueOf(pageResult.getSize()));
                    headers.add("X-Total-Pages", String.valueOf(pageResult.getTotalPages()));

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(dtos);
                });
    }
}