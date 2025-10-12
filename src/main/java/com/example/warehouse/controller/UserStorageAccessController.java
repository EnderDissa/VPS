package com.example.warehouse.controller;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.enumeration.AccessLevel;
import com.example.warehouse.service.UserStorageAccessService;
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
@RequestMapping("/api/v1/user-storage-access")
@Tag(name = "UserStorageAccess")
public class UserStorageAccessController {

    private final UserStorageAccessService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UserStorageAccessController(UserStorageAccessService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Grant access")
    public ResponseEntity<UserStorageAccessDTO> create(@Valid @RequestBody UserStorageAccessDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get access by id")
    public UserStorageAccessDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update access by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UserStorageAccessDTO dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete access by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List access entries with pagination and total count")
    public ResponseEntity<List<UserStorageAccessDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long storageId,
            @RequestParam(required = false) AccessLevel accessLevel,
            @RequestParam(required = false) Boolean active
    ) {
        var result = service.findPage(page, size, userId, storageId, accessLevel, active);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }
}
