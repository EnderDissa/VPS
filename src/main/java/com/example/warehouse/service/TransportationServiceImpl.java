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
                dto.itemId(), dto.fromStorageId(), dto.toStorageId());

        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.itemId()));

        Vehicle vehicle = vehicleRepository.findById(dto.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + dto.vehicleId()));

        User driver = userRepository.findById(dto.driverId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.driverId()));

        Storage fromStorage = storageRepository.findById(dto.fromStorageId())
                .orElseThrow(() -> new StorageNotFoundException("From storage not found with ID: " + dto.fromStorageId()));

        Storage toStorage = storageRepository.findById(dto.toStorageId())
                .orElseThrow(() -> new StorageNotFoundException("To storage not found with ID: " + dto.toStorageId()));

        if (dto.fromStorageId().equals(dto.toStorageId())) {
            throw new OperationNotAllowedException("From and to storage cannot be the same");
        }

        checkDriverAvailability(dto.driverId(), dto.scheduledDeparture(), dto.scheduledArrival());
        checkVehicleAvailability(dto.vehicleId(), dto.scheduledDeparture(), dto.scheduledArrival());

        Transportation transportation = transportationMapper.toEntity(dto);
        transportation.setItem(item);
        transportation.setVehicle(vehicle);
        transportation.setDriver(driver);
        transportation.setFromStorage(fromStorage);
        transportation.setToStorage(toStorage);
        transportation.setStatus(TransportStatus.PLANNED);

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

        Transportation existingTransportation = transportationRepository.findById(id)
                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id));

        if (isFinalStatus(existingTransportation.getStatus())) {
            throw new OperationNotAllowedException(
                    "Cannot update transportation with status: " + existingTransportation.getStatus());
        }

        updateRelatedEntities(existingTransportation, dto);

        existingTransportation.setStatus(dto.status());
        existingTransportation.setScheduledDeparture(dto.scheduledDeparture());
        existingTransportation.setScheduledArrival(dto.scheduledArrival());

        if (dto.status() == TransportStatus.IN_TRANSIT &&
                existingTransportation.getActualDeparture() == null) {
            existingTransportation.setActualDeparture(LocalDateTime.now());
        }

        if (dto.status() == TransportStatus.DELIVERED &&
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


    private void updateRelatedEntities(Transportation transportation, TransportationDTO dto) {
        if (!transportation.getItem().getId().equals(dto.itemId())) {
            Item item = itemRepository.findById(dto.itemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found with ID: " + dto.itemId()));
            transportation.setItem(item);
        }

        if (!transportation.getVehicle().getId().equals(dto.vehicleId())) {
            Vehicle vehicle = vehicleRepository.findById(dto.vehicleId())
                    .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + dto.vehicleId()));
            transportation.setVehicle(vehicle);
        }

        if (!transportation.getDriver().getId().equals(dto.driverId())) {
            User driver = userRepository.findById(dto.driverId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.driverId()));
            transportation.setDriver(driver);
        }

        if (!transportation.getFromStorage().getId().equals(dto.fromStorageId())) {
            Storage fromStorage = storageRepository.findById(dto.fromStorageId())
                    .orElseThrow(() -> new StorageNotFoundException("From storage not found with ID: " + dto.fromStorageId()));
            transportation.setFromStorage(fromStorage);
        }

        if (!transportation.getToStorage().getId().equals(dto.toStorageId())) {
            Storage toStorage = storageRepository.findById(dto.toStorageId())
                    .orElseThrow(() -> new StorageNotFoundException("To storage not found with ID: " + dto.toStorageId()));
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