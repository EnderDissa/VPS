package com.example.warehouse.controller;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.enumeration.BorrowStatus;
import com.example.warehouse.mapper.BorrowingMapper;
import com.example.warehouse.service.interfaces.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/borrowings")
@Tag(name = "Borrowings")
public class BorrowingController {

    private final BorrowingService service;
    private final BorrowingMapper mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public BorrowingController(BorrowingService service, BorrowingMapper borrowingMapper) {
        this.service = service;
        this.mapper = borrowingMapper;
    }

    @PostMapping
    @Operation(summary = "Create borrowing")
    public Mono<ResponseEntity<BorrowingDTO>> create(@Valid @RequestBody BorrowingDTO dto) {
        return service.create(dto)
                .map(mapper::toDTO)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get borrowing by id")
    public Mono<BorrowingDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(mapper::toDTO);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate borrowing")
    public Mono<ResponseEntity<Void>> activate(@PathVariable Long id) {
        return service.activate(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PutMapping("/{id}/extend")
    @Operation(summary = "Extend borrowing")
    public Mono<ResponseEntity<BorrowingDTO>> extend(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDueAt
    ) {
        return service.extend(id, newDueAt)
                .map(mapper::toDTO)
                .map(dto -> ResponseEntity.ok(dto));
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "Return borrowing")
    public Mono<ResponseEntity<BorrowingDTO>> returnBorrowing(@PathVariable Long id) {
        return service.returnBorrowing(id)
                .map(mapper::toDTO)
                .map(dto -> ResponseEntity.ok(dto));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel borrowing")
    public Mono<ResponseEntity<Void>> cancel(@PathVariable Long id) {
        return service.cancel(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping
    @Operation(summary = "List borrowings with pagination and total count")
    public Mono<ResponseEntity<List<BorrowingDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) BorrowStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        // Assuming service returns Flux for items and Mono for count
        Flux<Borrowing> borrowingsFlux = service.findBorrowingsByFilters(status, userId, itemId, from, to, pageable);
        Mono<Long> totalCountMono = service.countBorrowingsByFilters(status, userId, itemId, from, to);

        return borrowingsFlux
                .collectList()
                .zipWith(totalCountMono)
                .map(tuple -> {
                    List<Borrowing> borrowings = tuple.getT1();
                    Long totalCount = tuple.getT2();

                    // Map to DTOs
                    List<BorrowingDTO> borrowingDtos = borrowings.stream()
                            .map(mapper::toDTO)
                            .toList();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(totalCount));

                    return new ResponseEntity<>(borrowingDtos, headers, HttpStatus.OK);
                });
    }

    @GetMapping("/overdue")
    @Operation(summary = "List overdue borrowings with pagination and total count")
    public Mono<ResponseEntity<List<BorrowingDTO>>> overdue(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Flux<Borrowing> overdueBorrowingsFlux = service.findOverdueBorrowings(pageable);
        Mono<Long> totalCountMono = service.countOverdueBorrowings();

        return overdueBorrowingsFlux
                .collectList()
                .zipWith(totalCountMono)
                .map(tuple -> {
                    List<Borrowing> borrowings = tuple.getT1();
                    Long totalCount = tuple.getT2();

                    // Map to DTOs
                    List<BorrowingDTO> borrowingDtos = borrowings.stream()
                            .map(mapper::toDTO)
                            .toList();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(totalCount));

                    return new ResponseEntity<>(borrowingDtos, headers, HttpStatus.OK);
                });
    }
}