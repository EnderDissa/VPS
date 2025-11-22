package com.example.warehouse.controller;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;
import com.example.warehouse.mapper.KeepingMapper;
import com.example.warehouse.service.interfaces.KeepingService;
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
@RequestMapping("/api/v1/keeping")
@Tag(name = "Keeping")
public class KeepingController {

    private final KeepingService service;
    private final KeepingMapper mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public KeepingController(KeepingService service, KeepingMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Create keeping link")
    public Mono<ResponseEntity<KeepingDTO>> create(@Valid @RequestBody KeepingDTO dto) {
        return service.create(mapper.toEntity(dto))
                .map(mapper::toDTO)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get keeping by id")
    public Mono<KeepingDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(mapper::toDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update keeping by id")
    public Mono<ResponseEntity<Void>> update(@PathVariable Long id, @Valid @RequestBody KeepingDTO dto) {
        return service.update(id, mapper.toEntity(dto))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete keeping by id")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping
    @Operation(summary = "List keeping with pagination and total count")
    public Mono<ResponseEntity<List<KeepingDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) Long storageId,
            @RequestParam(required = false) Long itemId
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Flux<Keeping> keepingsFlux = service.findKeepingsByFilters(storageId, itemId, pageable);
        Mono<Long> totalCountMono = service.countKeepingsByFilters(storageId, itemId);

        return keepingsFlux
                .collectList()
                .zipWith(totalCountMono)
                .map(tuple -> {
                    List<Keeping> keepings = tuple.getT1();
                    Long totalCount = tuple.getT2();

                    // Map to DTOs
                    List<KeepingDTO> keepingDtos = keepings.stream()
                            .map(mapper::toDTO)
                            .toList();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(totalCount));

                    return new ResponseEntity<>(keepingDtos, headers, HttpStatus.OK);
                });
    }
}