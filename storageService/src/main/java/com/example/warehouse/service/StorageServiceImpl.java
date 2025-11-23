package com.example.warehouse.service;

import com.example.warehouse.entity.Storage;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.service.interfaces.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;

    @Override
    public Mono<Storage> create(Storage storage) {
        log.info("Creating new storage: {}", storage.getName());

        return storageRepository.existsByName(storage.getName())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Storage with name '{}' already exists", storage.getName());
                        return Mono.error(new DuplicateStorageException(
                                "Storage with name '" + storage.getName() + "' already exists"));
                    }
                    return storageRepository.save(storage);
                })
                .doOnSuccess(saved ->
                        log.info("Storage created successfully with ID: {}", saved.getId()))
                .doOnError(error ->
                        log.error("Failed to create storage: {}", error.getMessage()));
    }

    @Override
    public Mono<Storage> getById(Long id) {
        log.debug("Fetching storage by ID: {}", id);

        return storageRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new StorageNotFoundException("Storage not found with ID: " + id)))
                .doOnSuccess(storage ->
                        log.debug("Successfully fetched storage: {}", storage.getName()))
                .doOnError(error ->
                        log.error("Failed to fetch storage with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Storage> update(Long id, Storage storage) {
        log.info("Updating storage with ID: {}", id);

        return storageRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new StorageNotFoundException("Storage not found with ID: " + id)))
                .flatMap(existingStorage -> {
                    if (!existingStorage.getName().equals(storage.getName())) {
                        return storageRepository.existsByName(storage.getName())
                                .flatMap(exists -> {
                                    if (exists) {
                                        log.warn("Storage with name '{}' already exists", storage.getName());
                                        return Mono.error(new DuplicateStorageException(
                                                "Storage with name '" + storage.getName() + "' already exists"));
                                    }
                                    return updateStorage(existingStorage, storage);
                                });
                    }
                    return updateStorage(existingStorage, storage);
                })
                .doOnSuccess(updated ->
                        log.info("Storage with ID: {} updated successfully", id))
                .doOnError(error ->
                        log.error("Failed to update storage with ID {}: {}", id, error.getMessage()));
    }

    private Mono<Storage> updateStorage(Storage existing, Storage updated) {
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setCapacity(updated.getCapacity());
        return storageRepository.save(existing);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting storage with ID: {}", id);

        return storageRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new StorageNotFoundException("Storage not found with ID: " + id)))
                .flatMap(storage -> storageRepository.countKeepingsByStorageId(id))
                .flatMap(keepingCount -> {
                    if (keepingCount > 0) {
                        return Mono.error(new StorageNotEmptyException(
                                "Cannot delete storage with ID: " + id + ". It contains " + keepingCount + " items."));
                    }
                    return storageRepository.deleteById(id);
                })
                .doOnSuccess(v ->
                        log.info("Storage with ID: {} deleted successfully", id))
                .doOnError(error ->
                        log.error("Failed to delete storage with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Page<Storage>> findPage(int page, int size, String nameLike) {
        log.debug("Fetching storages page - page: {}, size: {}, nameLike: {}", page, size, nameLike);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (nameLike != null && !nameLike.trim().isEmpty()) {
            String searchTerm = nameLike.trim();

            return Mono.zip(
                    storageRepository.findByNameContainingIgnoreCase(searchTerm, pageable).collectList(),
                    storageRepository.countByNameContainingIgnoreCase(searchTerm)
            ).map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));

        } else {
            return Mono.zip(
                    storageRepository.findAllBy(pageable).collectList(),
                    storageRepository.count()
            ).map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
        }
    }
}