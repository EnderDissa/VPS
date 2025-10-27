package com.example.warehouse.service;

import com.example.warehouse.dto.StorageDTO;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.mapper.StorageMapper;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.service.interfaces.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;
    private final KeepingRepository keepingRepository;
    private final StorageMapper storageMapper;

    @Override
    @Transactional
    public StorageDTO create(StorageDTO dto) {
        log.info("Creating new storage: {}", dto.name());

        if (storageRepository.existsByName(dto.name())) {
            log.warn("Storage with name '{}' already exists", dto.name());
            throw new DuplicateStorageException("Storage with name '" + dto.name() + "' already exists");
        }

        Storage storage = storageMapper.toEntity(dto);

        Storage savedStorage = storageRepository.save(storage);
        log.info("Storage created successfully with ID: {}", savedStorage.getId());

        return storageMapper.toDTO(savedStorage);
    }

    @Override
    @Transactional(readOnly = true)
    public StorageDTO getById(Long id) {
        log.debug("Fetching storage by ID: {}", id);

        Storage storage = storageRepository.findById(id)
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));

        return storageMapper.toDTO(storage);
    }

    @Override
    @Transactional
    public void update(Long id, StorageDTO dto) {
        log.info("Updating storage with ID: {}", id);

        Storage existingStorage = storageRepository.findById(id)
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));

        if (!existingStorage.getName().equals(dto.name()) &&
                storageRepository.existsByName(dto.name())) {
            log.warn("Storage with name '{}' already exists", dto.name());
            throw new DuplicateStorageException("Storage with name '" + dto.name() + "' already exists");
        }

        existingStorage.setName(dto.name());
        existingStorage.setAddress(dto.address());
        existingStorage.setCapacity(dto.capacity());

        storageRepository.save(existingStorage);
        log.info("Storage with ID: {} updated successfully", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting storage with ID: {}", id);

        Storage storage = storageRepository.findById(id)
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));

        long keepingCount = keepingRepository.countByStorageId(id);
        if (keepingCount > 0) {
            throw new StorageNotEmptyException(
                    "Cannot delete storage with ID: " + id + ". It contains " + keepingCount + " items.");
        }

        storageRepository.deleteById(id);
        log.info("Storage with ID: {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StorageDTO> findPage(int page, int size, String nameLike) {
        log.debug("Fetching storages page - page: {}, size: {}, nameLike: {}", page, size, nameLike);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Storage> storagesPage;

        if (nameLike != null && !nameLike.trim().isEmpty()) {
            storagesPage = storageRepository.findByNameContainingIgnoreCase(nameLike.trim(), pageable);
        } else {
            storagesPage = storageRepository.findAll(pageable);
        }

        return storagesPage.map(storageMapper::toDTO);
    }
}
