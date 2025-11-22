package com.example.warehouse.service;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Keeping;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.entity.Item;
import com.example.warehouse.exception.KeepingNotFoundException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.DuplicateKeepingException;
import com.example.warehouse.mapper.KeepingMapper;
import com.example.warehouse.repository.KeepingRepository;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.service.interfaces.ItemService;
import com.example.warehouse.service.interfaces.KeepingService;
import com.example.warehouse.service.interfaces.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeepingServiceImpl implements KeepingService {

    private final KeepingRepository keepingRepository;
    private final StorageService storageService;
    private final ItemService itemService;

    @Override
    public Keeping create(Keeping keeping) {
        log.info("Creating new keeping record - storageId: {}, itemId: {}", keeping.getStorage().getId(), keeping.getItem().getId());

        Storage storage = storageService.getById(keeping.getStorage().getId());

        Item item = itemService.getById(keeping.getItem().getId());

        if (keepingRepository.existsByStorageIdAndItemId(keeping.getStorage().getId(), keeping.getItem().getId())) {
            throw new DuplicateKeepingException(
                    "Keeping record already exists for storage ID: " + keeping.getStorage().getId() +
                            " and item ID: " + keeping.getItem().getId());
        }

        keeping.setStorage(storage);
        keeping.setItem(item);

        Keeping savedKeeping = keepingRepository.save(keeping);
        log.info("Keeping record created successfully with ID: {}", savedKeeping.getId());

        return savedKeeping;
    }

    @Override
    public Keeping getById(Long id) {
        log.debug("Fetching keeping record by ID: {}", id);

        return keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));
    }

    @Override
    public void update(Long id, Keeping keeping) {
        log.info("Updating keeping record with ID: {}", id);

        Keeping existingKeeping = keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));

        if (!existingKeeping.getStorage().getId().equals(keeping.getStorage().getId())) {
            Storage storage = storageService.getById(keeping.getStorage().getId());
            existingKeeping.setStorage(storage);
        }

        if (!existingKeeping.getItem().getId().equals(keeping.getItem().getId())) {
            Item item = itemService.getById(keeping.getItem().getId());
            existingKeeping.setItem(item);

            if (keepingRepository.existsByStorageIdAndItemIdAndIdNot(
                    keeping.getStorage().getId(), keeping.getItem().getId(), id)) {
                throw new DuplicateKeepingException(
                        "Keeping record already exists for storage ID: " + keeping.getStorage().getId() +
                                " and item ID: " + keeping.getStorage().getId());
            }
        }

        existingKeeping.setQuantity(keeping.getQuantity());
        existingKeeping.setShelf(keeping.getShelf());

        keepingRepository.save(existingKeeping);
        log.info("Keeping record with ID: {} updated successfully", id);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting keeping record with ID: {}", id);

        if (!keepingRepository.existsById(id)) {
            throw new KeepingNotFoundException("Keeping record not found with ID: " + id);
        }

        keepingRepository.deleteById(id);
        log.info("Keeping record with ID: {} deleted successfully", id);
    }

    @Override
    public Page<Keeping> findPage(int page, int size, Long storageId, Long itemId) {
        log.debug("Fetching keeping records page - page: {}, size: {}, storageId: {}, itemId: {}",
                page, size, storageId, itemId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastUpdated"));

        Page<Keeping> keepingPage;

        if (storageId != null && itemId != null) {
            keepingPage = keepingRepository.findByStorageIdAndItemId(storageId, itemId, pageable);
        } else if (storageId != null) {
            keepingPage = keepingRepository.findByStorageId(storageId, pageable);
        } else if (itemId != null) {
            keepingPage = keepingRepository.findByItemId(itemId, pageable);
        } else {
            keepingPage = keepingRepository.findAll(pageable);
        }

        return keepingPage;
    }
}
