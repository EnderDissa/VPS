package com.example.warehouse.service;

import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.DuplicateSerialNumberException;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.service.interfaces.ItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Mono<Item> create(Item item) {
        log.info("Creating new item: {}", item.getName());

        return Mono.just(item)
                .filter(i -> i.getSerialNumber() == null || i.getSerialNumber().isEmpty() ||
                        !itemRepository.existsBySerialNumber(i.getSerialNumber()))
                .switchIfEmpty(Mono.error(new DuplicateSerialNumberException(
                        "Item with serial number '" + item.getSerialNumber() + "' already exists")))
                .flatMap(i -> {
                    i.setId(null);
                    return Mono.fromCallable(() -> itemRepository.save(i))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(savedItem -> log.info("Item created successfully with ID: {}", savedItem.getId()));
    }

    @Override
    public Mono<Item> getById(Long id) {
        log.debug("Fetching item by ID: {}", id);

        return Mono.fromCallable(() -> itemRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new ItemNotFoundException("Item not found with ID: " + id))));
    }

    @Override
    public Mono<Void> update(Long id, Item item) {
        return Mono.fromCallable(() -> itemRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new ItemNotFoundException("Item not found with ID: " + id));
                    }

                    Item existingItem = optional.get();

                    if (item.getSerialNumber() != null && !item.getSerialNumber().isEmpty() &&
                            !item.getSerialNumber().equals(existingItem.getSerialNumber())) {
                        if (itemRepository.existsBySerialNumber(item.getSerialNumber())) {
                            return Mono.error(new DuplicateSerialNumberException(
                                    "Item with serial number '" + item.getSerialNumber() + "' already exists"));
                        }
                    }

                    existingItem.setName(item.getName());
                    existingItem.setType(item.getType());
                    existingItem.setCondition(item.getCondition());
                    existingItem.setSerialNumber(item.getSerialNumber());
                    existingItem.setDescription(item.getDescription());

                    return Mono.fromCallable(() -> itemRepository.save(existingItem))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Item with ID: {} updated successfully", id))
                .then();
    }

    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromCallable(() -> itemRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ItemNotFoundException("Item not found with ID: " + id));
                    }
                    return Mono.fromCallable(() -> {
                        itemRepository.deleteById(id);
                        return null;
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Item with ID: {} deleted successfully", id))
                .then();
    }


    @Override
    public Flux<Item> findItemsByFilters(ItemType type, ItemCondition condition, Pageable pageable) {
        log.debug("Fetching items page - pageable: {}, type: {}, condition: {}",
                pageable, type, condition);


        return Mono.fromCallable(() -> {
                    if (type != null && condition != null) {
                        return itemRepository.findByTypeAndCondition(type, condition, pageable);
                    } else if (type != null) {
                        return itemRepository.findByType(type, pageable);
                    } else if (condition != null) {
                        return itemRepository.findByCondition(condition, pageable);
                    } else {
                        return itemRepository.findAll(pageable);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    @Override
    public Mono<Long> countItemsByFilters(ItemType type, ItemCondition condition) {
        log.debug("Counting items - type: {}, condition: {}", type, condition);


        return Mono.fromCallable(() -> {
                    if (type != null && condition != null) {
                        return itemRepository.findByTypeAndCondition(type, condition, PageRequest.of(0, 1)).getTotalElements();


                    } else if (type != null) {
                        return itemRepository.findByType(type, PageRequest.of(0, 1)).getTotalElements();

                    } else if (condition != null) {
                        return itemRepository.findByCondition(condition, PageRequest.of(0, 1)).getTotalElements();

                    } else {
                        return itemRepository.count();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }


    @Override
    public Flux<Item> findAvailable(LocalDateTime from, LocalDateTime to, Long storageId,
                                    ItemType type, ItemCondition condition, Long cursor, int limit) {
        log.debug("Finding available items - from: {}, to: {}, storageId: {}, type: {}, condition: {}, cursor: {}, limit: {}",
                from, to, storageId, type, condition, cursor, limit);


        return Mono.fromCallable(() -> {

                    PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id"));
                    if (cursor != null) {
                        if (type != null && condition != null) {
                            return itemRepository.findByIdGreaterThanAndTypeAndCondition(
                                    cursor, type, condition, pageable);
                        } else if (type != null) {
                            return itemRepository.findByIdGreaterThanAndType(
                                    cursor, type, pageable);
                        } else if (condition != null) {
                            return itemRepository.findByIdGreaterThanAndCondition(
                                    cursor, condition, pageable);
                        } else {
                            return itemRepository.findByIdGreaterThan(
                                    cursor, pageable);
                        }
                    } else {
                        if (type != null && condition != null) {
                            return itemRepository.findByTypeAndCondition(
                                    type, condition, pageable);
                        } else if (type != null) {
                            return itemRepository.findByType(
                                    type, pageable);
                        } else if (condition != null) {
                            return itemRepository.findByCondition(
                                    condition, pageable);
                        } else {
                            return itemRepository.findAll(pageable);
                        }
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }
}