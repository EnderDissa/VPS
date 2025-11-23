package com.example.warehouse.controller;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.mapper.StorageMapper;
import com.example.warehouse.service.interfaces.StorageService;
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
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/storages")
@Tag(name = "Storages")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService service;
    private final StorageMapper mapper;

    @PostMapping
    @Operation(summary = "Create storage")
    public Mono<ResponseEntity<StorageDTO>> create(@Valid @RequestBody StorageDTO dto) {
        return service.create(mapper.toEntity(dto))
                .map(storage -> ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(storage)))
                .onErrorResume(DuplicateStorageException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .doOnSuccess(response -> log.debug("Storage created successfully"))
                .doOnError(error -> log.error("Error creating storage: {}", error.getMessage()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get storage by id")
    public Mono<ResponseEntity<StorageDTO>> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(storage -> ResponseEntity.ok(mapper.toDTO(storage)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(StorageNotFoundException.class, e ->
                        Mono.just(ResponseEntity.notFound().build()))
                .doOnSuccess(response -> {
                    if (response.getStatusCode() == HttpStatus.OK) {
                        log.debug("Storage found with ID: {}", id);
                    }
                })
                .doOnError(error -> log.error("Error fetching storage with ID {}: {}", id, error.getMessage()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update storage by id")
    public Mono<ResponseEntity<Void>> update(@PathVariable Long id, @Valid @RequestBody StorageDTO dto) {
        return service.update(id, mapper.toEntity(dto))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(StorageNotFoundException.class, e ->
                        Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(DuplicateStorageException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .doOnSuccess(response -> log.debug("Storage updated with ID: {}", id))
                .doOnError(error -> log.error("Error updating storage with ID {}: {}", id, error.getMessage()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete storage by id")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(StorageNotFoundException.class, e ->
                        Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(StorageNotEmptyException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(null)))
                .doOnSuccess(response -> log.debug("Storage deleted with ID: {}", id))
                .doOnError(error -> log.error("Error deleting storage with ID {}: {}", id, error.getMessage()));
    }

    @GetMapping
    @Operation(summary = "List storages with pagination and total count")
    public Mono<ResponseEntity<List<StorageDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String nameLike
    ) {
        return service.findPage(page, size, nameLike)
                .map(pageResult -> {
                    List<StorageDTO> content = pageResult.getContent().stream()
                            .map(mapper::toDTO)
                            .collect(Collectors.toList());

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(pageResult.getTotalElements()));
                    headers.add("X-Total-Pages", String.valueOf(pageResult.getTotalPages()));
                    headers.add("X-Current-Page", String.valueOf(page));
                    headers.add("X-Page-Size", String.valueOf(size));

                    return new ResponseEntity<>(content, headers, HttpStatus.OK);
                })
                .doOnSuccess(response -> log.debug("Fetched storages page: {}, size: {}", page, size))
                .doOnError(error -> log.error("Error fetching storages page: {}", error.getMessage()));
    }
}