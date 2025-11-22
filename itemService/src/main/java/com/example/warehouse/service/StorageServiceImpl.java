//package com.example.warehouse.service;
//
//import com.example.warehouse.entity.Storage;
//import com.example.warehouse.exception.DuplicateStorageException;
//import com.example.warehouse.exception.StorageNotEmptyException;
//import com.example.warehouse.exception.StorageNotFoundException;
//import com.example.warehouse.repository.StorageRepository;
//import com.example.warehouse.service.interfaces.StorageService;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class StorageServiceImpl implements StorageService {
//
//    private final StorageRepository storageRepository;
//
//    @Override
//    public Storage create(Storage storage) {
//        log.info("Creating new storage: {}", storage.getName());
//
//        if (storageRepository.existsByName(storage.getName())) {
//            log.warn("Storage with name '{}' already exists", storage.getName());
//            throw new DuplicateStorageException("Storage with name '" + storage.getName() + "' already exists");
//        }
//
//        Storage savedStorage = storageRepository.save(storage);
//        log.info("Storage created successfully with ID: {}", savedStorage.getId());
//
//        return savedStorage;
//    }
//
//    @Override
//    public Storage getById(Long id) {
//        log.debug("Fetching storage by ID: {}", id);
//
//        return storageRepository.findById(id)
//                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));
//    }
//
//    @Override
//    public void update(Long id, Storage storage) {
//        log.info("Updating storage with ID: {}", id);
//
//        Storage existingStorage = storageRepository.findById(id)
//                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));
//
//        if (!existingStorage.getName().equals(storage.getName()) &&
//                storageRepository.existsByName(storage.getName())) {
//            log.warn("Storage with name '{}' already exists", storage.getName());
//            throw new DuplicateStorageException("Storage with name '" + storage.getName() + "' already exists");
//        }
//
//        existingStorage.setName(storage.getName());
//        existingStorage.setAddress(storage.getAddress());
//        existingStorage.setCapacity(storage.getCapacity());
//
//        storageRepository.save(existingStorage);
//        log.info("Storage with ID: {} updated successfully", id);
//    }
//
//    @Override
//    public void delete(Long id) {
//        log.info("Deleting storage with ID: {}", id);
//
//        storageRepository.findById(id)
//                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + id));
//
//        long keepingCount = storageRepository.countKeepingsByStorageId(id);
//        if (keepingCount > 0) {
//            throw new StorageNotEmptyException(
//                    "Cannot delete storage with ID: " + id + ". It contains " + keepingCount + " items.");
//        }
//
//        storageRepository.deleteById(id);
//        log.info("Storage with ID: {} deleted successfully", id);
//    }
//
//    @Override
//    public Page<Storage> findPage(int page, int size, String nameLike) {
//        log.debug("Fetching storages page - page: {}, size: {}, nameLike: {}", page, size, nameLike);
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//
//        Page<Storage> storagesPage;
//
//        if (nameLike != null && !nameLike.trim().isEmpty()) {
//            storagesPage = storageRepository.findByNameContainingIgnoreCase(nameLike.trim(), pageable);
//        } else {
//            storagesPage = storageRepository.findAll(pageable);
//        }
//
//        return storagesPage;
//    }
//}
