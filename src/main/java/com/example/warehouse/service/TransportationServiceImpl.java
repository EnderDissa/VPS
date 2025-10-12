package com.example.warehouse.service;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.enumeration.TransportStatus;
import com.example.warehouse.exception.TransportationNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.VehicleNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.mapper.TransportationMapper;
import com.example.warehouse.repository.TransportationRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.VehicleRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.repository.StorageRepository;
import com.example.warehouse.service.interfaces.TransportationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransportationServiceImpl implements TransportationService {

    private final TransportationRepository transportationRepository;
    private final ItemRepository itemRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final TransportationMapper transportationMapper;

    @Override
    @Transactional
    public TransportationDTO create(TransportationDTO dto) {
        log.info("Creating new transportation for item ID: {} from storage {} to storage {}",
                dto.getItemId(), dto.getFromStorageId(), dto.getToStorageId());

        // Проверяем существование связанных сущностей
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.getItemId()));

        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + dto.getVehicleId()));

        User driver = userRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.getDriverId()));

        Storage fromStorage = storageRepository.findById(dto.getFromStorageId())
                .orElseThrow(() -> new StorageNotFoundException("From storage not found with ID: " + dto.getFromStorageId()));

        Storage toStorage = storageRepository.findById(dto.getToStorageId())
                .orElseThrow(() -> new StorageNotFoundException("To storage not found with ID: " + dto.getToStorageId()));

        // Проверяем, что from и to storage разные
        if (dto.getFromStorageId().equals(dto.getToStorageId())) {
            throw new OperationNotAllowedException("From and to storage cannot be the same");
        }

        // Проверяем доступность водителя и транспортного средства
        checkDriverAvailability(dto.getDriverId(), dto.getScheduledDeparture(), dto.getScheduledArrival());
        checkVehicleAvailability(dto.getVehicleId(), dto.getScheduledDeparture(), dto.getScheduledArrival());

        // Создаем entity
        Transportation transportation = transportationMapper.toEntity(dto);
        transportation.setItem(item);
        transportation.setVehicle(vehicle);
        transportation.setDriver(driver);
        transportation.setFromStorage(fromStorage);
        transportation.setToStorage(toStorage);
        transportation.setStatus(TransportStatus.PLANNED); // Новые перевозки создаются как PLANNED

        // Сохраняем
        Transportation savedTransportation = transportationRepository.save(transportation);
        log.info("Transportation created successfully with ID: {}", savedTransportation.getId());

        return transportationMapper.toDTO(savedTransportation);
    }

    @Override
    @Transactional(readOnly = true)
    public TransportationDTO getById(Long id) {
        log.debug("Fetching transportation by ID: {}", id);

        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id));

        return transportationMapper.toDTO(transportation);
    }

    @Override
    @Transactional
    public void update(Long id, TransportationDTO dto) {
        log.info("Updating transportation with ID: {}", id);

        // Находим существующую перевозку
        Transportation existingTransportation = transportationRepository.findById(id)
                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id));

        // Проверяем, что перевозка не в финальном статусе
        if (isFinalStatus(existingTransportation.getStatus())) {
            throw new OperationNotAllowedException(
                    "Cannot update transportation with status: " + existingTransportation.getStatus());
        }

        // Проверяем и обновляем связанные сущности
        updateRelatedEntities(existingTransportation, dto);

        // Обновляем остальные поля
        existingTransportation.setStatus(dto.getStatus());
        existingTransportation.setScheduledDeparture(dto.getScheduledDeparture());
        existingTransportation.setScheduledArrival(dto.getScheduledArrival());

        // Если статус изменился на IN_PROGRESS, устанавливаем actualDeparture
        if (dto.getStatus() == TransportStatus.IN_TRANSIT &&
                existingTransportation.getActualDeparture() == null) {
            existingTransportation.setActualDeparture(LocalDateTime.now());
        }

        // Если статус изменился на DELIVERED, устанавливаем actualArrival
        if (dto.getStatus() == TransportStatus.DELIVERED &&
                existingTransportation.getActualArrival() == null) {
            existingTransportation.setActualArrival(LocalDateTime.now());
        }

        transportationRepository.save(existingTransportation);
        log.info("Transportation with ID: {} updated successfully", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting transportation with ID: {}", id);

        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id));

        // Проверяем, что перевозка не в финальном статусе
        if (isFinalStatus(transportation.getStatus())) {
            throw new OperationNotAllowedException(
                    "Cannot delete transportation with status: " + transportation.getStatus());
        }

        transportationRepository.deleteById(id);
        log.info("Transportation with ID: {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransportationDTO> findPage(int page, int size, TransportStatus status, Long itemId,
                                            Long fromStorageId, Long toStorageId) {
        log.debug("Fetching transportations page - page: {}, size: {}, status: {}, itemId: {}, fromStorageId: {}, toStorageId: {}",
                page, size, status, itemId, fromStorageId, toStorageId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Transportation> transportationsPage;

        if (status != null && itemId != null && fromStorageId != null && toStorageId != null) {
            transportationsPage = transportationRepository.findByStatusAndItemIdAndFromStorageIdAndToStorageId(
                    status, itemId, fromStorageId, toStorageId, pageable);
        } else if (status != null && itemId != null) {
            transportationsPage = transportationRepository.findByStatusAndItemId(status, itemId, pageable);
        } else if (status != null && fromStorageId != null) {
            transportationsPage = transportationRepository.findByStatusAndFromStorageId(status, fromStorageId, pageable);
        } else if (status != null && toStorageId != null) {
            transportationsPage = transportationRepository.findByStatusAndToStorageId(status, toStorageId, pageable);
        } else if (itemId != null && fromStorageId != null) {
            transportationsPage = transportationRepository.findByItemIdAndFromStorageId(itemId, fromStorageId, pageable);
        } else if (status != null) {
            transportationsPage = transportationRepository.findByStatus(status, pageable);
        } else if (itemId != null) {
            transportationsPage = transportationRepository.findByItemId(itemId, pageable);
        } else if (fromStorageId != null) {
            transportationsPage = transportationRepository.findByFromStorageId(fromStorageId, pageable);
        } else if (toStorageId != null) {
            transportationsPage = transportationRepository.findByToStorageId(toStorageId, pageable);
        } else {
            transportationsPage = transportationRepository.findAll(pageable);
        }

        return transportationsPage.map(transportationMapper::toDTO);
    }

    // Вспомогательные методы

    private void updateRelatedEntities(Transportation transportation, TransportationDTO dto) {
        // Обновляем item если изменился
        if (!transportation.getItem().getId().equals(dto.getItemId())) {
            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.getItemId()));
            transportation.setItem(item);
        }

        // Обновляем vehicle если изменился
        if (!transportation.getVehicle().getId().equals(dto.getVehicleId())) {
            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + dto.getVehicleId()));
            transportation.setVehicle(vehicle);
        }

        // Обновляем driver если изменился
        if (!transportation.getDriver().getId().equals(dto.getDriverId())) {
            User driver = userRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.getDriverId()));
            transportation.setDriver(driver);
        }

        // Обновляем fromStorage если изменился
        if (!transportation.getFromStorage().getId().equals(dto.getFromStorageId())) {
            Storage fromStorage = storageRepository.findById(dto.getFromStorageId())
                    .orElseThrow(() -> new StorageNotFoundException("From storage not found with ID: " + dto.getFromStorageId()));
            transportation.setFromStorage(fromStorage);
        }

        // Обновляем toStorage если изменился
        if (!transportation.getToStorage().getId().equals(dto.getToStorageId())) {
            Storage toStorage = storageRepository.findById(dto.getToStorageId())
                    .orElseThrow(() -> new StorageNotFoundException("To storage not found with ID: " + dto.getToStorageId()));
            transportation.setToStorage(toStorage);
        }
    }

    private void checkDriverAvailability(Long driverId, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            boolean isAvailable = transportationRepository.isDriverAvailable(driverId, start, end);
            if (!isAvailable) {
                throw new OperationNotAllowedException("Driver is not available during the specified time period");
            }
        }
    }

    private void checkVehicleAvailability(Long vehicleId, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            boolean isAvailable = transportationRepository.isVehicleAvailable(vehicleId, start, end);
            if (!isAvailable) {
                throw new OperationNotAllowedException("Vehicle is not available during the specified time period");
            }
        }
    }

    private boolean isFinalStatus(TransportStatus status) {
        return status == TransportStatus.DELIVERED || status == TransportStatus.CANCELLED;
    }

    // Дополнительные методы

    @Transactional
    public TransportationDTO startTransportation(Long id) {
        log.info("Starting transportation with ID: {}", id);

        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id));

        if (transportation.getStatus() != TransportStatus.PLANNED) {
            throw new OperationNotAllowedException(
                    "Cannot start transportation with status: " + transportation.getStatus());
        }

        transportation.setStatus(TransportStatus.IN_TRANSIT);
        transportation.setActualDeparture(LocalDateTime.now());

        Transportation updatedTransportation = transportationRepository.save(transportation);
        log.info("Transportation with ID: {} started successfully", id);

        return transportationMapper.toDTO(updatedTransportation);
    }

    @Transactional
    public TransportationDTO completeTransportation(Long id) {
        log.info("Completing transportation with ID: {}", id);

        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id));

        if (transportation.getStatus() != TransportStatus.IN_TRANSIT) {
            throw new OperationNotAllowedException(
                    "Cannot complete transportation with status: " + transportation.getStatus());
        }

        transportation.setStatus(TransportStatus.DELIVERED);
        transportation.setActualArrival(LocalDateTime.now());

        Transportation updatedTransportation = transportationRepository.save(transportation);
        log.info("Transportation with ID: {} completed successfully", id);

        return transportationMapper.toDTO(updatedTransportation);
    }

    @Transactional
    public TransportationDTO cancelTransportation(Long id) {
        log.info("Canceling transportation with ID: {}", id);

        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id));

        if (isFinalStatus(transportation.getStatus())) {
            throw new OperationNotAllowedException(
                    "Cannot cancel transportation with status: " + transportation.getStatus());
        }

        transportation.setStatus(TransportStatus.CANCELLED);

        Transportation updatedTransportation = transportationRepository.save(transportation);
        log.info("Transportation with ID: {} cancelled successfully", id);

        return transportationMapper.toDTO(updatedTransportation);
    }

    @Transactional(readOnly = true)
    public Page<TransportationDTO> findOverdueTransportations(int page, int size) {
        log.debug("Fetching overdue transportations - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "scheduledArrival"));
        LocalDateTime now = LocalDateTime.now();

        Page<Transportation> overdueTransportations = transportationRepository.findOverdueTransportations(now, pageable);

        return overdueTransportations.map(transportationMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public long countByStatus(TransportStatus status) {
        log.debug("Counting transportations with status: {}", status);
        return transportationRepository.countByStatus(status);
    }
}