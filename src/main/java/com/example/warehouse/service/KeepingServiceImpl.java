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
import com.example.warehouse.service.interfaces.KeepingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final StorageRepository storageRepository;
    private final ItemRepository itemRepository;
    private final KeepingMapper keepingMapper;

    @Override
    
    public KeepingDTO create(KeepingDTO dto) {
        log.info("Creating new keeping record - storageId: {}, itemId: {}", dto.storageId(), dto.itemId());

        Storage storage = storageRepository.findById(dto.storageId())
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + dto.storageId()));

        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.itemId()));

        if (keepingRepository.existsByStorageIdAndItemId(dto.storageId(), dto.itemId())) {
            throw new DuplicateKeepingException(
                    "Keeping record already exists for storage ID: " + dto.storageId() +
                            " and item ID: " + dto.itemId());
        }

        Keeping keeping = keepingMapper.toEntity(dto);
        keeping.setStorage(storage);
        keeping.setItem(item);

        Keeping savedKeeping = keepingRepository.save(keeping);
        log.info("Keeping record created successfully with ID: {}", savedKeeping.getId());

        return keepingMapper.toDTO(savedKeeping);
    }

    @Override
    public KeepingDTO getById(Long id) {
        log.debug("Fetching keeping record by ID: {}", id);

        Keeping keeping = keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));

        return keepingMapper.toDTO(keeping);
    }

    @Override
    
    public void update(Long id, KeepingDTO dto) {
        log.info("Updating keeping record with ID: {}", id);

        Keeping existingKeeping = keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));

        if (!existingKeeping.getStorage().getId().equals(dto.storageId())) {
            Storage storage = storageRepository.findById(dto.storageId())
                    .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + dto.storageId()));
            existingKeeping.setStorage(storage);
        }

        if (!existingKeeping.getItem().getId().equals(dto.itemId())) {
            Item item = itemRepository.findById(dto.itemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.itemId()));
            existingKeeping.setItem(item);

            if (keepingRepository.existsByStorageIdAndItemIdAndIdNot(
                    dto.storageId(), dto.itemId(), id)) {
                throw new DuplicateKeepingException(
                        "Keeping record already exists for storage ID: " + dto.storageId() +
                                " and item ID: " + dto.itemId());
            }
        }

        existingKeeping.setQuantity(dto.quantity());
        existingKeeping.setShelf(dto.shelf());

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
    public Page<KeepingDTO> findPage(int page, int size, Long storageId, Long itemId) {
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

        return keepingPage.map(keepingMapper::toDTO);
    }
}
