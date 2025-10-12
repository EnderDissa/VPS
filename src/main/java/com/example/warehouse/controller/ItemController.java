package com.example.warehouse.controller;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.service.interfaces.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create item")
    public ResponseEntity<ItemDTO> create(@Valid @RequestBody ItemDTO dto) {
        ItemDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by id")
    public ItemDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ItemDTO dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List items with pagination and total count")
    public ResponseEntity<List<ItemDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) ItemCondition condition
    ) {
        var pageResult = service.findPage(page, size, type, condition);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(pageResult.getTotalElements()));
        return new ResponseEntity<>(pageResult.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/availability")
    @Operation(summary = "Availability infinite feed")
    public List<ItemDTO> availability(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long storageId,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") @Min(1) @Max(50) int limit
    ) {
        return service.findAvailable(from, to, storageId, type, condition, cursor, limit);
    }
}
