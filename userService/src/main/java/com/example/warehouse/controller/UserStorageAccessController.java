package com.example.warehouse.controller;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import com.example.warehouse.mapper.UserStorageAccessMapper;
import com.example.warehouse.service.interfaces.UserStorageAccessService;
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
@RequestMapping("/api/v1/user-storage-access")
@Tag(name = "UserStorageAccess")
public class UserStorageAccessController {

    private final UserStorageAccessService service;
    private final UserStorageAccessMapper mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UserStorageAccessController(UserStorageAccessService service, UserStorageAccessMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Grant access")
    public Mono<ResponseEntity<UserStorageAccessDTO>> create(@Valid @RequestBody UserStorageAccessDTO dto) {
        return service.create(mapper.toEntity(dto))
                .map(mapper::toDTO)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get access by id")
    public Mono<UserStorageAccessDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(mapper::toDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update access by id")
    public Mono<ResponseEntity<Void>> update(@PathVariable Long id, @Valid @RequestBody UserStorageAccessDTO dto) {
        return service.update(id, mapper.toEntity(dto))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete access by id")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping
    @Operation(summary = "List access entries with pagination and total count")
    public Mono<ResponseEntity<List<UserStorageAccessDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long storageId,
            @RequestParam(required = false) String accessLevel,
            @RequestParam(required = false) Boolean active
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Flux<UserStorageAccess> accessesFlux = service.findUserStorageAccessesByFilters(userId, storageId, accessLevel, active, pageable);
        Mono<Long> totalCountMono = service.countUserStorageAccessesByFilters(userId, storageId, accessLevel, active);

        return accessesFlux
                .collectList()
                .zipWith(totalCountMono)
                .map(tuple -> {
                    List<UserStorageAccess> accesses = tuple.getT1();
                    Long totalCount = tuple.getT2();

                    List<UserStorageAccessDTO> accessDtos = accesses.stream()
                            .map(mapper::toDTO)
                            .toList();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(totalCount));

                    return new ResponseEntity<>(accessDtos, headers, HttpStatus.OK);
                });
    }
}