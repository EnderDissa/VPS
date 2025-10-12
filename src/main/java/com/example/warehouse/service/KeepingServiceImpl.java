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
@Transactional
@RequiredArgsConstructor
public class KeepingServiceImpl implements KeepingService {

    private final KeepingRepository keepingRepository;
    private final StorageRepository storageRepository;
    private final ItemRepository itemRepository;
    private final KeepingMapper keepingMapper;

    @Override
    @Transactional
    public KeepingDTO create(KeepingDTO dto) {
        log.info("Creating new keeping record - storageId: {}, itemId: {}", dto.getStorageId(), dto.getItemId());

        // Проверяем существование storage
        Storage storage = storageRepository.findById(dto.getStorageId())
                .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + dto.getStorageId()));

        // Проверяем существование item
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.getItemId()));

        // Проверяем уникальность комбинации storage + item
        if (keepingRepository.existsByStorageIdAndItemId(dto.getStorageId(), dto.getItemId())) {
            throw new DuplicateKeepingException(
                    "Keeping record already exists for storage ID: " + dto.getStorageId() +
                            " and item ID: " + dto.getItemId());
        }

        // Создаем entity
        Keeping keeping = keepingMapper.toEntity(dto);
        keeping.setStorage(storage);
        keeping.setItem(item);

        // Сохраняем
        Keeping savedKeeping = keepingRepository.save(keeping);
        log.info("Keeping record created successfully with ID: {}", savedKeeping.getId());

        return keepingMapper.toDTO(savedKeeping);
    }

    @Override
    @Transactional(readOnly = true)
    public KeepingDTO getById(Long id) {
        log.debug("Fetching keeping record by ID: {}", id);

        Keeping keeping = keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));

        return keepingMapper.toDTO(keeping);
    }

    @Override
    @Transactional
    public void update(Long id, KeepingDTO dto) {
        log.info("Updating keeping record with ID: {}", id);

        // Находим существующую запись
        Keeping existingKeeping = keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));

        // Проверяем, изменился ли storage
        if (!existingKeeping.getStorage().getId().equals(dto.getStorageId())) {
            Storage storage = storageRepository.findById(dto.getStorageId())
                    .orElseThrow(() -> new StorageNotFoundException("Storage not found with ID: " + dto.getStorageId()));
            existingKeeping.setStorage(storage);
        }

        // Проверяем, изменился ли item
        if (!existingKeeping.getItem().getId().equals(dto.getItemId())) {
            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.getItemId()));
            existingKeeping.setItem(item);

            // Проверяем уникальность новой комбинации storage + item
            if (keepingRepository.existsByStorageIdAndItemIdAndIdNot(
                    dto.getStorageId(), dto.getItemId(), id)) {
                throw new DuplicateKeepingException(
                        "Keeping record already exists for storage ID: " + dto.getStorageId() +
                                " and item ID: " + dto.getItemId());
            }
        }

        // Обновляем остальные поля
        existingKeeping.setQuantity(dto.getQuantity());
        existingKeeping.setShelf(dto.getShelf());

        keepingRepository.save(existingKeeping);
        log.info("Keeping record with ID: {} updated successfully", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting keeping record with ID: {}", id);

        if (!keepingRepository.existsById(id)) {
            throw new KeepingNotFoundException("Keeping record not found with ID: " + id);
        }

        keepingRepository.deleteById(id);
        log.info("Keeping record with ID: {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
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

    // Дополнительные методы

    @Transactional(readOnly = true)
    public KeepingDTO findByStorageAndItem(Long storageId, Long itemId) {
        log.debug("Finding keeping record by storageId: {} and itemId: {}", storageId, itemId);

        Keeping keeping = keepingRepository.findByStorageIdAndItemId(storageId, itemId)
                .orElseThrow(() -> new KeepingNotFoundException(
                        "Keeping record not found for storage ID: " + storageId + " and item ID: " + itemId));

        return keepingMapper.toDTO(keeping);
    }

    @Transactional
    public KeepingDTO updateQuantity(Long id, Integer newQuantity) {
        log.info("Updating quantity to {} for keeping record ID: {}", newQuantity, id);

        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Keeping keeping = keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));

        keeping.setQuantity(newQuantity);
        Keeping updatedKeeping = keepingRepository.save(keeping);

        log.info("Quantity updated successfully for keeping record ID: {}", id);
        return keepingMapper.toDTO(updatedKeeping);
    }

    @Transactional
    public KeepingDTO addQuantity(Long id, Integer quantityToAdd) {
        log.info("Adding quantity {} to keeping record ID: {}", quantityToAdd, id);

        Keeping keeping = keepingRepository.findById(id)
                .orElseThrow(() -> new KeepingNotFoundException("Keeping record not found with ID: " + id));

        int newQuantity = keeping.getQuantity() + quantityToAdd;
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Resulting quantity must be positive");
        }

        keeping.setQuantity(newQuantity);
        Keeping updatedKeeping = keepingRepository.save(keeping);

        log.info("Quantity added successfully for keeping record ID: {}. New quantity: {}", id, newQuantity);
        return keepingMapper.toDTO(updatedKeeping);
    }

    @Transactional(readOnly = true)
    public long countByStorageId(Long storageId) {
        log.debug("Counting keeping records for storage ID: {}", storageId);
        return keepingRepository.countByStorageId(storageId);
    }

    @Transactional(readOnly = true)
    public long countByItemId(Long itemId) {
        log.debug("Counting keeping records for item ID: {}", itemId);
        return keepingRepository.countByItemId(itemId);
    }
}
