package com.example.warehouse.service;

import com.example.warehouse.entity.Storage;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.service.interfaces.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;

    @Override
    public Mono<Storage> create(Storage storage) {
        log.info("Creating new storage: {}", storage.getName());

        return Mono.fromCallable(() -> {
                    if (storageRepository.existsByName(storage.getName())) {
                        log.warn("Storage with name '{}' already exists", storage.getName());
                        throw new DuplicateStorageException(
                                "Storage with name '" + storage.getName() + "' already exists");
                    }
                    return storageRepository.save(storage);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(saved -> log.info("Storage created successfully with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to create storage: {}", error.getMessage()));
    }

    @Override
    public Mono<Storage> getById(Long id) {
        return Mono.fromCallable(() ->
                        storageRepository.findById(id)
                                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(storage -> log.debug("Successfully fetched storage: {}", storage.getName()))
                .doOnError(error -> log.error("Failed to fetch storage with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Storage> update(Long id, Storage storage) {
        log.info("Updating storage with ID: {}", id);

        return Mono.fromCallable(() -> {
                    Storage existing = storageRepository.findById(id)
                            .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));

                    if (!existing.getName().equals(storage.getName()) &&
                            storageRepository.existsByName(storage.getName())) {
                        log.warn("Storage with name '{}' already exists", storage.getName());
                        throw new DuplicateStorageException(
                                "Storage with name '" + storage.getName() + "' already exists");
                    }

                    existing.setName(storage.getName());
                    existing.setAddress(storage.getAddress());
                    existing.setCapacity(storage.getCapacity());

                    return storageRepository.save(existing);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(updated -> log.info("Storage with ID: {} updated successfully", id))
                .doOnError(error -> log.error("Failed to update storage with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromCallable(() -> {
                    Storage storage = storageRepository.findById(id)
                            .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));

                    long keepingCount = storageRepository.countKeepingsByStorageId(id);
                    if (keepingCount > 0) {
                        throw new StorageNotEmptyException(
                                "Cannot delete storage with ID: " + id + ". It contains " + keepingCount + " items.");
                    }

                    storageRepository.deleteById(id);
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .doOnSuccess(v -> log.info("Storage with ID: {} deleted successfully", id))
                .doOnError(error -> log.error("Failed to delete storage with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Page<Storage>> findPage(int page, int size, String nameLike) {
        log.debug("Fetching storages page - page: {}, size: {}, nameLike: {}", page, size, nameLike);

        return Mono.fromCallable(() -> {
                    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

                    if (nameLike != null && !nameLike.trim().isEmpty()) {
                        String term = nameLike.trim();
                        var pageResult = storageRepository.findByNameContainingIgnoreCase(term, pageable);
                        long total = storageRepository.countByNameContainingIgnoreCase(term);
                        return new PageImpl<>(pageResult.getContent(), pageable, total);
                    } else {
                        return storageRepository.findAll(pageable);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}