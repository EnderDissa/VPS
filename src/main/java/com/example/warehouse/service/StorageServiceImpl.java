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
        log.info("Creating new storage: {}", dto.getName());

        // Проверяем уникальность имени (опционально)
        if (storageRepository.existsByName(dto.getName())) {
            log.warn("Storage with name '{}' already exists", dto.getName());
             throw new DuplicateStorageException("Storage with name '" + dto.getName() + "' already exists");
        }

        // Создаем entity
        Storage storage = storageMapper.toEntity(dto);

        // Сохраняем
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

        // Находим существующий storage
        Storage existingStorage = storageRepository.findById(id)
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));

        // Проверяем уникальность имени (если оно изменилось)
        if (!existingStorage.getName().equals(dto.getName()) &&
                storageRepository.existsByName(dto.getName())) {
            log.warn("Storage with name '{}' already exists", dto.getName());
             throw new DuplicateStorageException("Storage with name '" + dto.getName() + "' already exists");
        }

        // Обновляем поля
        existingStorage.setName(dto.getName());
        existingStorage.setAddress(dto.getAddress());
        existingStorage.setCapacity(dto.getCapacity());

        storageRepository.save(existingStorage);
        log.info("Storage with ID: {} updated successfully", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting storage with ID: {}", id);

        Storage storage = storageRepository.findById(id)
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));

        // Проверяем, есть ли связанные записи в keeping
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

//    // Дополнительные методы
//
//    @Transactional(readOnly = true)
//    public StorageDTO getByName(String name) {
//        log.debug("Fetching storage by name: {}", name);
//
//        Storage storage = storageRepository.findByName(name)
//                .orElseThrow(() -> new StorageNotFoundException("Storage not found with name: " + name));
//
//        return storageMapper.toDTO(storage);
//    }
//
//    @Transactional(readOnly = true)
//    public List<StorageDTO> findAll() {
//        log.debug("Fetching all storages");
//
//        List<Storage> storages = storageRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
//
//        return storages.stream()
//                .map(storageMapper::toDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<StorageDTO> findByCapacityGreaterThanEqual(Integer minCapacity) {
//        log.debug("Fetching storages with capacity >= {}", minCapacity);
//
//        List<Storage> storages = storageRepository.findByCapacityGreaterThanEqual(minCapacity);
//
//        return storages.stream()
//                .map(storageMapper::toDTO)
//                .collect(Collectors.toList());
//    }

//    @Transactional(readOnly = true)
//    public long count() {
//        log.debug("Counting all storages");
//        return storageRepository.count();
//    }
//
//    @Transactional(readOnly = true)
//    public boolean existsByName(String name) {
//        return storageRepository.existsByName(name);
//    }
//
//    @Transactional(readOnly = true)
//    public Integer getTotalCapacity() {
//        log.debug("Calculating total capacity of all storages");
//
//        Integer totalCapacity = storageRepository.getTotalCapacity();
//        return totalCapacity != null ? totalCapacity : 0;
//    }
//
//    @Transactional(readOnly = true)
//    public Integer getUsedCapacity(Long storageId) {
//        log.debug("Calculating used capacity for storage ID: {}", storageId);
//
//        // Получаем общее количество товаров на складе
//        Integer totalQuantity = keepingRepository.getTotalQuantityInStorage(storageId);
//        return totalQuantity != null ? totalQuantity : 0;
//    }
//
//    @Transactional(readOnly = true)
//    public Integer getAvailableCapacity(Long storageId) {
//        log.debug("Calculating available capacity for storage ID: {}", storageId);
//
//        Storage storage = storageRepository.findById(storageId)
//                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + storageId));
//
//        Integer usedCapacity = getUsedCapacity(storageId);
//        return storage.getCapacity() - usedCapacity;
//    }
}
