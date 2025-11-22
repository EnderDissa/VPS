package com.example.warehouse.controller;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.enumeration.MaintenanceStatus;
import com.example.warehouse.mapper.ItemMaintenanceMapper;
import com.example.warehouse.service.interfaces.ItemMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance")
public class ItemMaintenanceController {

    private final ItemMaintenanceService service;
    private final ItemMaintenanceMapper mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ItemMaintenanceController(ItemMaintenanceService service, ItemMaintenanceMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Create maintenance record")
    public Mono<ResponseEntity<ItemMaintenanceDTO>> create(@Valid @RequestBody ItemMaintenanceDTO dto) {
        return service.create(mapper.toEntity(dto))
                .map(mapper::toDTO)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get maintenance by id")
    public Mono<ItemMaintenanceDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(mapper::toDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update maintenance by id")
    public Mono<ResponseEntity<Void>> update(@PathVariable Long id, @Valid @RequestBody ItemMaintenanceDTO dto) {
        return service.update(id, mapper.toEntity(dto))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete maintenance by id")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping
    @Operation(summary = "List maintenance with pagination and total count")
    public Mono<ResponseEntity<List<ItemMaintenanceDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) MaintenanceStatus status
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Flux<ItemMaintenance> maintenancesFlux = service.findMaintenancesByFilters(itemId, status, pageable);
        Mono<Long> totalCountMono = service.countMaintenancesByFilters(itemId, status);

        return maintenancesFlux
                .collectList()
                .zipWith(totalCountMono)
                .map(tuple -> {
                    List<ItemMaintenance> maintenances = tuple.getT1();
                    Long totalCount = tuple.getT2();

                    // Map to DTOs
                    List<ItemMaintenanceDTO> maintenanceDtos = maintenances.stream()
                            .map(mapper::toDTO)
                            .toList();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(totalCount));

                    return new ResponseEntity<>(maintenanceDtos, headers, HttpStatus.OK);
                });
    }
}