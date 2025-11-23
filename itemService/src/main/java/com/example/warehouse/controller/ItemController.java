package com.example.warehouse.controller;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.mapper.ItemMapper;
import com.example.warehouse.service.interfaces.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/items")
@Tag(name = "Items")
public class ItemController {

    private final ItemService service;
    private final ItemMapper mapper;

    public ItemController(ItemService service, ItemMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }


    @PostMapping
    @Operation(summary = "Create item")
    public Mono<ItemDTO> create(@Valid @RequestBody ItemDTO dto) {
        Mono<Item> created = service.create(mapper.toEntity(dto));
        return created.map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by id")
    public Mono<ItemDTO> getById(@PathVariable Long id) {
        return service.getById(id).map(mapper::toDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item by id")
    public Mono<Void> update(@PathVariable Long id, @Valid @RequestBody ItemDTO dto) {
        return service.update(id, mapper.toEntity(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item by id")
    public Mono<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @GetMapping
    @Operation(summary = "List items with pagination and total count")
    public Mono<ResponseEntity<Page<ItemDTO>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) ItemCondition condition
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Flux<Item> itemFlux = service.findItemsByFilters(type, condition, pageable);
        Mono<Long> totalCountMono = service.countItemsByFilters(type, condition);

        return itemFlux
                .collectList()
                .zipWith(totalCountMono)
                .map(tuple -> {
                    List<Item> items = tuple.getT1();
                    Long totalCount = tuple.getT2();

                    Page<Item> itemPage = new PageImpl<>(items, pageable, totalCount);

                    List<ItemDTO> itemDtos = items.stream()
                            .map(mapper::toDTO)
                            .toList();

                    Page<ItemDTO> itemDtoPage = new PageImpl<>(itemDtos, pageable, totalCount);

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Total-Count", String.valueOf(totalCount));

                    return new ResponseEntity<>(itemDtoPage, headers, HttpStatus.OK);
                });
    }

    @GetMapping("/availability")
    @Operation(summary = "Availability infinite feed")
    public Flux<ItemDTO> availability(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long storageId,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") @Min(1) @Max(50) int limit
    ) {
        return service.findAvailable(from, to, storageId, type, condition, cursor, limit)
                .map(mapper::toDTO);
    }
}