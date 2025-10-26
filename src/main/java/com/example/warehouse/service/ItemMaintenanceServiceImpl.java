package com.example.warehouse.service;

import com.example.warehouse.dto.ItemMaintenanceDTO;
import com.example.warehouse.entity.ItemMaintenance;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.MaintenanceStatus;
import com.example.warehouse.exception.ItemMaintenanceNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.mapper.ItemMaintenanceMapper;
import com.example.warehouse.repository.ItemMaintenanceRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.service.interfaces.ItemMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemMaintenanceServiceImpl implements ItemMaintenanceService {

    private final ItemMaintenanceRepository itemMaintenanceRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMaintenanceMapper itemMaintenanceMapper;

    @Override
    @Transactional
    public ItemMaintenanceDTO create(ItemMaintenanceDTO dto) {
        log.info("Creating new item maintenance for item ID: {}", dto.itemId());

        // Проверяем существование item
        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.itemId()));

        // Проверяем существование technician
        User technician = userRepository.findById(dto.technicianId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.technicianId()));

        // Создаем entity
        ItemMaintenance maintenance = itemMaintenanceMapper.toEntity(dto);
        maintenance.setItem(item);
        maintenance.setTechnician(technician);

        // Сохраняем
        ItemMaintenance savedMaintenance = itemMaintenanceRepository.save(maintenance);
        log.info("Item maintenance created successfully with ID: {}", savedMaintenance.getId());

        return itemMaintenanceMapper.toDTO(savedMaintenance);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemMaintenanceDTO getById(Long id) {
        log.debug("Fetching item maintenance by ID: {}", id);

        ItemMaintenance maintenance = itemMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));

        return itemMaintenanceMapper.toDTO(maintenance);
    }

    @Override
    @Transactional
    public void update(Long id, ItemMaintenanceDTO dto) {
        log.info("Updating item maintenance with ID: {}", id);

        // Находим существующую запись
        ItemMaintenance existingMaintenance = itemMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));

        // Проверяем item если он изменился
        if (!existingMaintenance.getItem().getId().equals(dto.itemId())) {
            Item item = itemRepository.findById(dto.itemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.itemId()));
            existingMaintenance.setItem(item);
        }

        // Проверяем technician если он изменился
        if (!existingMaintenance.getTechnician().getId().equals(dto.technicianId())) {
            User technician = userRepository.findById(dto.technicianId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.technicianId()));
            existingMaintenance.setTechnician(technician);
        }

        // Обновляем остальные поля
        existingMaintenance.setMaintenanceDate(dto.maintenanceDate());
        existingMaintenance.setNextMaintenanceDate(dto.nextMaintenanceDate());
        existingMaintenance.setCost(dto.cost());
        existingMaintenance.setDescription(dto.description());
        existingMaintenance.setStatus(dto.status());

        itemMaintenanceRepository.save(existingMaintenance);
        log.info("Item maintenance with ID: {} updated successfully", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting item maintenance with ID: {}", id);

        if (!itemMaintenanceRepository.existsById(id)) {
            throw new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id);
        }

        itemMaintenanceRepository.deleteById(id);
        log.info("Item maintenance with ID: {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemMaintenanceDTO> findPage(int page, int size, Long itemId, MaintenanceStatus status) {
        log.debug("Fetching item maintenance page - page: {}, size: {}, itemId: {}, status: {}",
                page, size, itemId, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maintenanceDate"));

        Page<ItemMaintenance> maintenancePage;

        if (itemId != null && status != null) {
            maintenancePage = itemMaintenanceRepository.findByItemIdAndStatus(itemId, status, pageable);
        } else if (itemId != null) {
            maintenancePage = itemMaintenanceRepository.findByItemId(itemId, pageable);
        } else if (status != null) {
            maintenancePage = itemMaintenanceRepository.findByStatus(status, pageable);
        } else {
            maintenancePage = itemMaintenanceRepository.findAll(pageable);
        }

        return maintenancePage.map(itemMaintenanceMapper::toDTO);
    }

    // Дополнительные методы

    @Transactional(readOnly = true)
    public Page<ItemMaintenanceDTO> findByTechnician(Long technicianId, int page, int size) {
        log.debug("Fetching item maintenance by technician ID: {}", technicianId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maintenanceDate"));
        Page<ItemMaintenance> maintenancePage = itemMaintenanceRepository.findByTechnicianId(technicianId, pageable);

        return maintenancePage.map(itemMaintenanceMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public long countByStatus(MaintenanceStatus status) {
        log.debug("Counting item maintenance records with status: {}", status);
        return itemMaintenanceRepository.countByStatus(status);
    }

    @Transactional
    public void updateStatus(Long id, MaintenanceStatus status) {
        log.info("Updating status to {} for item maintenance ID: {}", status, id);

        ItemMaintenance maintenance = itemMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));

        maintenance.setStatus(status);
        itemMaintenanceRepository.save(maintenance);
        log.info("Status updated successfully for item maintenance ID: {}", id);
    }
}