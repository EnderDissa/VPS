package com.example.warehouse.service;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.DuplicateSerialNumberException;
import com.example.warehouse.mapper.ItemMapper;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.service.interfaces.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    
    public ItemDTO create(ItemDTO dto) {
        log.info("Creating new item: {}", dto.name());

        if (dto.serialNumber() != null && !dto.serialNumber().isEmpty()) {
            if (itemRepository.existsBySerialNumber(dto.serialNumber())) {
                throw new DuplicateSerialNumberException(
                        "Item with serial number '" + dto.serialNumber() + "' already exists");
            }
        }

        Item item = itemMapper.toEntity(dto);
        item.setId(null);
        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully with ID: {}", savedItem.getId());

        return itemMapper.toDTO(savedItem);
    }

    @Override
    public ItemDTO getById(Long id) {
        log.debug("Fetching item by ID: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + id));

        return itemMapper.toDTO(item);
    }

    @Override
    
    public void update(Long id, ItemDTO dto) {
        log.info("Updating item with ID: {}", id);

        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + id));

        if (dto.serialNumber() != null && !dto.serialNumber().isEmpty() &&
                !dto.serialNumber().equals(existingItem.getSerialNumber())) {
            if (itemRepository.existsBySerialNumber(dto.serialNumber())) {
                throw new DuplicateSerialNumberException(
                        "Item with serial number '" + dto.serialNumber() + "' already exists");
            }
        }

        existingItem.setName(dto.name());
        existingItem.setType(dto.type());
        existingItem.setCondition(dto.condition());
        existingItem.setSerialNumber(dto.serialNumber());
        existingItem.setDescription(dto.description());

        itemRepository.save(existingItem);
        log.info("Item with ID: {} updated successfully", id);
    }

    @Override
    
    public void delete(Long id) {
        log.info("Deleting item with ID: {}", id);

        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException("Item not found with ID: " + id);
        }

        itemRepository.deleteById(id);
        log.info("Item with ID: {} deleted successfully", id);
    }

    @Override
    public Page<ItemDTO> findPage(int page, int size, ItemType type, ItemCondition condition) {
        log.debug("Fetching items page - page: {}, size: {}, type: {}, condition: {}",
                page, size, type, condition);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Item> itemsPage;

        if (type != null && condition != null) {
            itemsPage = itemRepository.findByTypeAndCondition(type, condition, pageable);
        } else if (type != null) {
            itemsPage = itemRepository.findByType(type, pageable);
        } else if (condition != null) {
            itemsPage = itemRepository.findByCondition(condition, pageable);
        } else {
            itemsPage = itemRepository.findAll(pageable);
        }

        Page<Item> resPage = (Page<Item>) itemsPage;

        return resPage.map(itemMapper::toDTO);
    }

    @Override
    public List<ItemDTO> findAvailable(LocalDateTime from, LocalDateTime to, Long storageId,
                                       ItemType type, ItemCondition condition, Long cursor, int limit) {
        log.debug("Finding available items - from: {}, to: {}, storageId: {}, type: {}, condition: {}, cursor: {}, limit: {}",
                from, to, storageId, type, condition, cursor, limit);

        Page<Item> availableItems;

        if (cursor != null) {
            if (type != null && condition != null) {
                availableItems = itemRepository.findByIdGreaterThanAndTypeAndCondition(
                        cursor, type, condition, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            } else if (type != null) {
                availableItems = itemRepository.findByIdGreaterThanAndType(
                        cursor, type, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            } else if (condition != null) {
                availableItems = itemRepository.findByIdGreaterThanAndCondition(
                        cursor, condition, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            } else {
                availableItems = itemRepository.findByIdGreaterThan(
                        cursor, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            }
        } else {
            if (type != null && condition != null) {
                availableItems = itemRepository.findByTypeAndCondition(
                        type, condition, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            } else if (type != null) {
                availableItems = itemRepository.findByType(
                        type, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            } else if (condition != null) {
                availableItems = itemRepository.findByCondition(
                        condition, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            } else {
                availableItems = itemRepository.findAll(
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")));
            }
        }

        return availableItems.stream()
                .map(itemMapper::toDTO)
                .collect(Collectors.toList());
    }
}
