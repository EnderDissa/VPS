package com.example.warehouse.controller;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.enumeration.BorrowStatus;
import com.example.warehouse.service.interfaces.BorrowingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/v1/borrowings")
@Tag(name = "Borrowings")
public class BorrowingController {

    private final BorrowingService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public BorrowingController(BorrowingService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create borrowing")
    public ResponseEntity<BorrowingDTO> create(@Valid @RequestBody BorrowingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get borrowing by id")
    public BorrowingDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate borrowing")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/extend")
    @Operation(summary = "Extend borrowing")
    public ResponseEntity<BorrowingDTO> extend(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDueAt
    ) {
        return ResponseEntity.ok(service.extend(id, newDueAt));
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "Return borrowing")
    public ResponseEntity<BorrowingDTO> returnBorrowing(@PathVariable Long id) {
        return ResponseEntity.ok(service.returnBorrowing(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel borrowing")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List borrowings with pagination and total count")
    public ResponseEntity<List<BorrowingDTO>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) BorrowStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        var result = service.findPage(page, size, status, userId, itemId, from, to);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/overdue")
    @Operation(summary = "List overdue borrowings with pagination and total count")
    public ResponseEntity<List<BorrowingDTO>> overdue(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        var result = service.findOverdue(page, size);
        var headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }
}
