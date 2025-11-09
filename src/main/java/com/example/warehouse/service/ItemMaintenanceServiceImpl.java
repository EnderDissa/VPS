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
@RequiredArgsConstructor
public class ItemMaintenanceServiceImpl implements ItemMaintenanceService {

    private final ItemMaintenanceRepository itemMaintenanceRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;

    @Override
    
    public ItemMaintenance create(ItemMaintenance maintenance) {
        log.info("Creating new item maintenance for item ID: {}", maintenance.getItem().getId());

        Item item = itemService.getById(maintenance.getItem().getId());

        User technician = userService.getUserById(maintenance.getTechnician().getId());

        maintenance.setItem(item);
        maintenance.setTechnician(technician);

        ItemMaintenance savedMaintenance = itemMaintenanceRepository.save(maintenance);
        log.info("Item maintenance created successfully with ID: {}", savedMaintenance.getId());

        return savedMaintenance;
    }

    @Override
    public ItemMaintenance getById(Long id) {
        log.debug("Fetching item maintenance by ID: {}", id);

        return itemMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));
    }

    @Override
    
    public void update(Long id, ItemMaintenance maintenance) {
        log.info("Updating item maintenance with ID: {}", id);

        ItemMaintenance existingMaintenance = itemMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));

        if (!existingMaintenance.getItem().getId().equals(maintenance.getItem().getId())) {
            Item item = itemService.getById(maintenance.getItem().getId());
            existingMaintenance.setItem(item);
        }

        if (!existingMaintenance.getTechnician().getId().equals(maintenance.getTechnician().getId())) {
            User technician = userService.getUserById(maintenance.getTechnician().getId());
            existingMaintenance.setTechnician(technician);
        }

        existingMaintenance.setMaintenanceDate(maintenance.getMaintenanceDate());
        existingMaintenance.setNextMaintenanceDate(maintenance.getNextMaintenanceDate());
        existingMaintenance.setCost(maintenance.getCost());
        existingMaintenance.setDescription(maintenance.getDescription());
        existingMaintenance.setStatus(maintenance.getStatus());

        itemMaintenanceRepository.save(existingMaintenance);
        log.info("Item maintenance with ID: {} updated successfully", id);
    }

    @Override
    
    public void delete(Long id) {
        log.info("Deleting item maintenance with ID: {}", id);

        if (!itemMaintenanceRepository.existsById(id)) {
            throw new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id);
        }

        itemMaintenanceRepository.deleteById(id);
        log.info("Item maintenance with ID: {} deleted successfully", id);
    }

    @Override
    public Page<ItemMaintenance> findPage(int page, int size, Long itemId, MaintenanceStatus status) {
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

        return maintenancePage;
    }


    public Page<ItemMaintenance> findByTechnician(Long technicianId, int page, int size) {
        log.debug("Fetching item maintenance by technician ID: {}", technicianId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maintenanceDate"));

        return itemMaintenanceRepository.findByTechnicianId(technicianId, pageable);
    }

    public long countByStatus(MaintenanceStatus status) {
        log.debug("Counting item maintenance records with status: {}", status);
        return itemMaintenanceRepository.countByStatus(status);
    }

    
    public void updateStatus(Long id, MaintenanceStatus status) {
        log.info("Updating status to {} for item maintenance ID: {}", status, id);

        ItemMaintenance maintenance = itemMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));

        maintenance.setStatus(status);
        itemMaintenanceRepository.save(maintenance);
        log.info("Status updated successfully for item maintenance ID: {}", id);
    }
}