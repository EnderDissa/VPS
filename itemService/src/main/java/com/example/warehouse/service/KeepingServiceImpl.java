package com.example.warehouse.service;

import com.example.warehouse.client.StorageServiceClient;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.Keeping;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.exception.DuplicateKeepingException;
import com.example.warehouse.exception.KeepingNotFoundException;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.service.interfaces.ItemService;
import com.example.warehouse.service.interfaces.KeepingService;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeepingServiceImpl implements KeepingService {

    private final KeepingRepository keepingRepository;
    private final StorageServiceClient storageService;
    private final ItemService itemService;

    @Override
    public Mono<Keeping> create(Keeping keeping) {
        log.info("Creating new keeping record - storageId: {}, itemId: {}", keeping.getStorage().getId(), keeping.getItem().getId());

        return storageService.getById(keeping.getStorage().getId())
                .flatMap(storage -> itemService.getById(keeping.getItem().getId())
                        .flatMap(item -> Mono.fromCallable(() -> keepingRepository.existsByStorageIdAndItemId(keeping.getStorage().getId(), keeping.getItem().getId()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new DuplicateKeepingException(
                                                "Keeping record already exists for storage ID: " + keeping.getStorage().getId() +
                                                        " and item ID: " + keeping.getItem().getId()));
                                    }

                                    keeping.setStorage(storage);
                                    keeping.setItem(item);

                                    return Mono.fromCallable(() -> keepingRepository.save(keeping))
                                            .subscribeOn(Schedulers.boundedElastic());
                                })))
                .doOnSuccess(savedKeeping -> log.info("Keeping record created successfully with ID: {}", savedKeeping.getId()));
    }

    @Override
    public Mono<Keeping> getById(Long id) {
        log.debug("Fetching keeping record by ID: {}", id);

        return Mono.fromCallable(() -> keepingRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new KeepingNotFoundException("Keeping record not found with ID: " + id))));
    }

    @Override
    public Mono<Void> update(Long id, Keeping keeping) {
        return Mono.fromCallable(() -> keepingRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new KeepingNotFoundException("Keeping record not found with ID: " + id));
                    }

                    Keeping existingKeeping = optional.get();

                    Mono<Storage> storageMono = Mono.just(keeping.getStorage().getId())
                            .filter(storageId -> !existingKeeping.getStorage().getId().equals(storageId))
                            .flatMap(storageId -> storageService.getById(storageId))
                            .switchIfEmpty(Mono.just(existingKeeping.getStorage()))
                            .doOnNext(storage -> existingKeeping.setStorage(storage));

                    Mono<Item> itemMono = Mono.just(keeping.getItem().getId())
                            .filter(itemId -> !existingKeeping.getItem().getId().equals(itemId))
                            .flatMap(itemId -> itemService.getById(itemId))
                            .switchIfEmpty(Mono.just(existingKeeping.getItem()))
                            .doOnNext(item -> {
                                existingKeeping.setItem(item);
                                // Check for duplicate if item changes
                                if (!existingKeeping.getItem().getId().equals(keeping.getItem().getId())) {
                                    // This check is a bit complex in reactive context, might need to adjust
                                    // For now, we'll do it after we have the item
                                }
                            });

                    return storageMono
                            .then(itemMono)
                            .flatMap(v -> {
                                // Check for duplicate after we know the new item ID
                                if (!existingKeeping.getItem().getId().equals(keeping.getItem().getId())) {
                                    return Mono.fromCallable(() -> keepingRepository.existsByStorageIdAndItemIdAndIdNot(
                                                    keeping.getStorage().getId(), keeping.getItem().getId(), id))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .flatMap(exists -> {
                                                if (exists) {
                                                    return Mono.error(new DuplicateKeepingException(
                                                            "Keeping record already exists for storage ID: " + keeping.getStorage().getId() +
                                                                    " and item ID: " + keeping.getItem().getId()));
                                                }
                                                return Mono.empty();
                                            });
                                }
                                return Mono.empty();
                            })
                            .then(Mono.fromCallable(() -> {
                                existingKeeping.setQuantity(keeping.getQuantity());
                                existingKeeping.setShelf(keeping.getShelf());
                                return keepingRepository.save(existingKeeping);
                            }).subscribeOn(Schedulers.boundedElastic()));
                })
                .doOnSuccess(v -> log.info("Keeping record with ID: {} updated successfully", id))
                .then();
    }

    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromCallable(() -> keepingRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new KeepingNotFoundException("Keeping record not found with ID: " + id));
                    }
                    return Mono.fromCallable(() -> {
                        keepingRepository.deleteById(id);
                        return null;
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Keeping record with ID: {} deleted successfully", id))
                .then();
    }

    // New methods for reactive pagination
    @Override
    public Flux<Keeping> findKeepingsByFilters(Long storageId, Long itemId, Pageable pageable) {
        log.debug("Fetching keeping records page - pageable: {}, storageId: {}, itemId: {}",
                pageable, storageId, itemId);

        return Mono.fromCallable(() -> {
                    if (storageId != null && itemId != null) {
                        return keepingRepository.findByStorageIdAndItemId(storageId, itemId, pageable);
                    } else if (storageId != null) {
                        return keepingRepository.findByStorageId(storageId, pageable);
                    } else if (itemId != null) {
                        return keepingRepository.findByItemId(itemId, pageable);
                    } else {
                        return keepingRepository.findAll(pageable);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    @Override
    public Mono<Long> countKeepingsByFilters(Long storageId, Long itemId) {
        log.debug("Counting keeping records - storageId: {}, itemId: {}", storageId, itemId);

        return Mono.fromCallable(() -> {
                    if (storageId != null && itemId != null) {
                        return keepingRepository.countByStorageIdAndItemId(storageId, itemId);
                    } else if (storageId != null) {
                        return keepingRepository.countByStorageId(storageId);
                    } else if (itemId != null) {
                        return keepingRepository.countByItemId(itemId);
                    } else {
                        return keepingRepository.count();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}