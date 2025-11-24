package com.example.warehouse.service;

import com.example.warehouse.client.ItemServiceClient;
import com.example.warehouse.client.UserServiceClient;
import com.example.warehouse.entity.*;
import com.example.warehouse.enumeration.TransportStatus;
import com.example.warehouse.exception.*;
import com.example.warehouse.repository.TransportationRepository;
import com.example.warehouse.service.interfaces.StorageService;
import com.example.warehouse.service.interfaces.TransportationService;
import com.example.warehouse.service.interfaces.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportationServiceImpl implements TransportationService {

    private final TransportationRepository transportationRepository;
    private final ItemServiceClient itemServiceClient;
    private final UserServiceClient userServiceClient;
    private final VehicleService vehicleService;
    private final StorageService storageService;

    @Override
    public Mono<Transportation> create(Transportation transportation) {
        log.info("Creating new transportation for item ID: {} from storage {} to storage {}",
                transportation.getItem().getId(),
                transportation.getFromStorage().getId(),
                transportation.getToStorage().getId());

        // Проверяем, что склады разные
        if (transportation.getFromStorage().getId().equals(transportation.getToStorage().getId())) {
            return Mono.error(new OperationNotAllowedException("From and to storage cannot be the same"));
        }

        // Получаем связанные сущности через клиенты (они уже реактивные)
        return Mono.zip(
                        itemServiceClient.getItemById(transportation.getItem().getId())
                                .switchIfEmpty(Mono.error(new ItemNotFoundException("Item not found with ID: " + transportation.getItem().getId()))),
                        userServiceClient.getUserById(transportation.getDriver().getId())
                                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with ID: " + transportation.getDriver().getId()))),
                        vehicleService.getById(transportation.getVehicle().getId())
                                .switchIfEmpty(Mono.error(new VehicleNotFoundException("Vehicle not found with ID: " + transportation.getVehicle().getId()))),
                        storageService.getById(transportation.getFromStorage().getId())
                                .switchIfEmpty(Mono.error(new StorageNotFoundException("From storage not found with ID: " + transportation.getFromStorage().getId()))),
                        storageService.getById(transportation.getToStorage().getId())
                                .switchIfEmpty(Mono.error(new StorageNotFoundException("To storage not found with ID: " + transportation.getToStorage().getId())))
                )
                .flatMap(tuple -> {
                    Item item = tuple.getT1();
                    User driver = tuple.getT2();
                    Vehicle vehicle = tuple.getT3();
                    Storage fromStorage = tuple.getT4();
                    Storage toStorage = tuple.getT5();

                    // Проверяем доступность (оставляем как реактивный вызов — клиент или локальная заглушка)
                    return checkAvailability(driver.getId(), vehicle.getId(),
                            transportation.getScheduledDeparture(), transportation.getScheduledArrival())
                            .then(Mono.fromCallable(() -> {
                                transportation.setItem(item);
                                transportation.setDriver(driver);
                                transportation.setVehicle(vehicle);
                                transportation.setFromStorage(fromStorage);
                                transportation.setToStorage(toStorage);
                                transportation.setStatus(TransportStatus.PLANNED);

                                return transportationRepository.save(transportation);
                            }))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(saved -> log.info("Transportation created successfully with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to create transportation: {}", error.getMessage()));
    }

    @Override
    public Mono<Transportation> getById(Long id) {
        log.debug("Fetching transportation by ID: {}", id);

        return Mono.fromCallable(() ->
                        transportationRepository.findById(id)
                                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(transportation ->
                        log.debug("Successfully fetched transportation: {}", transportation.getId()))
                .doOnError(error ->
                        log.error("Failed to fetch transportation with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Transportation> update(Long id, Transportation transportation) {
        log.info("Updating transportation with ID: {}", id);

        return Mono.fromCallable(() ->
                        transportationRepository.findById(id)
                                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(existingTransportation -> {
                    if (isFinalStatus(existingTransportation.getStatus())) {
                        return Mono.error(new OperationNotAllowedException(
                                "Cannot update transportation with status: " + existingTransportation.getStatus()));
                    }

                    return updateRelatedEntities(existingTransportation, transportation)
                            .flatMap(updated -> Mono.fromCallable(() -> {
                                updated.setStatus(transportation.getStatus());
                                updated.setScheduledDeparture(transportation.getScheduledDeparture());
                                updated.setScheduledArrival(transportation.getScheduledArrival());

                                if (transportation.getStatus() == TransportStatus.IN_TRANSIT &&
                                        updated.getActualDeparture() == null) {
                                    updated.setActualDeparture(LocalDateTime.now());
                                }

                                if (transportation.getStatus() == TransportStatus.DELIVERED &&
                                        updated.getActualArrival() == null) {
                                    updated.setActualArrival(LocalDateTime.now());
                                }

                                return transportationRepository.save(updated);
                            }))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(updated -> log.info("Transportation with ID: {} updated successfully", id))
                .doOnError(error -> log.error("Failed to update transportation with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting transportation with ID: {}", id);

        return Mono.fromCallable(() ->
                        transportationRepository.findById(id)
                                .orElseThrow(() -> new TransportationNotFoundException("Transportation not found with ID: " + id))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(transportation -> {
                    if (isFinalStatus(transportation.getStatus())) {
                        return Mono.error(new OperationNotAllowedException(
                                "Cannot delete transportation with status: " + transportation.getStatus()));
                    }
                    return Mono.fromRunnable(() -> transportationRepository.deleteById(id))
                            .subscribeOn(Schedulers.boundedElastic())
                            .then();
                })
                .doOnSuccess(v -> log.info("Transportation with ID: {} deleted successfully", id))
                .doOnError(error -> log.error("Failed to delete transportation with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Page<Transportation>> findPage(int page, int size, TransportStatus status,
                                               Long itemId, Long fromStorageId, Long toStorageId) {
        log.debug("Fetching transportations page - page: {}, size: {}, status: {}, itemId: {}, fromStorageId: {}, toStorageId: {}",
                page, size, status, itemId, fromStorageId, toStorageId);

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return Mono.fromCallable(() -> {
                    if (status != null && itemId != null && fromStorageId != null && toStorageId != null) {
                        return transportationRepository.findByStatusAndItemIdAndFromStorageIdAndToStorageId(
                                status, itemId, fromStorageId, toStorageId, pageable);
                    } else if (status != null && itemId != null) {
                        return transportationRepository.findByStatusAndItemId(status, itemId, pageable);
                    } else if (status != null && fromStorageId != null) {
                        return transportationRepository.findByStatusAndFromStorageId(status, fromStorageId, pageable);
                    } else if (status != null && toStorageId != null) {
                        return transportationRepository.findByStatusAndToStorageId(status, toStorageId, pageable);
                    } else if (itemId != null && fromStorageId != null) {
                        return transportationRepository.findByItemIdAndFromStorageId(itemId, fromStorageId, pageable);
                    } else if (status != null) {
                        return transportationRepository.findByStatus(status, pageable);
                    } else if (itemId != null) {
                        return transportationRepository.findByItemId(itemId, pageable);
                    } else if (fromStorageId != null) {
                        return transportationRepository.findByFromStorageId(fromStorageId, pageable);
                    } else if (toStorageId != null) {
                        return transportationRepository.findByToStorageId(toStorageId, pageable);
                    } else {
                        return transportationRepository.findAll(pageable);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(pageResult ->
                        log.debug("Fetched {} transportations", pageResult.getNumberOfElements()))
                .doOnError(error ->
                        log.error("Failed to fetch transportations page: {}", error.getMessage()));
    }

    // === Вспомогательные методы ===

    private Mono<Transportation> updateRelatedEntities(Transportation existing, Transportation updated) {
        return Mono.zip(
                updateItemIfNeeded(existing, updated),
                updateDriverIfNeeded(existing, updated),
                updateVehicleIfNeeded(existing, updated),
                updateFromStorageIfNeeded(existing, updated),
                updateToStorageIfNeeded(existing, updated)
        ).thenReturn(existing);
    }

    private Mono<Void> updateItemIfNeeded(Transportation existing, Transportation updated) {
        if (!existing.getItem().getId().equals(updated.getItem().getId())) {
            return itemServiceClient.getItemById(updated.getItem().getId())
                    .switchIfEmpty(Mono.error(new ItemNotFoundException("Item not found with ID: " + updated.getItem().getId())))
                    .doOnNext(existing::setItem)
                    .then();
        }
        return Mono.empty();
    }

    private Mono<Void> updateDriverIfNeeded(Transportation existing, Transportation updated) {
        if (!existing.getDriver().getId().equals(updated.getDriver().getId())) {
            return userServiceClient.getUserById(updated.getDriver().getId())
                    .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with ID: " + updated.getDriver().getId())))
                    .doOnNext(existing::setDriver)
                    .then();
        }
        return Mono.empty();
    }

    private Mono<Void> updateVehicleIfNeeded(Transportation existing, Transportation updated) {
        if (!existing.getVehicle().getId().equals(updated.getVehicle().getId())) {
            return vehicleService.getById(updated.getVehicle().getId())
                    .switchIfEmpty(Mono.error(new VehicleNotFoundException("Vehicle not found with ID: " + updated.getVehicle().getId())))
                    .doOnNext(existing::setVehicle)
                    .then();
        }
        return Mono.empty();
    }

    private Mono<Void> updateFromStorageIfNeeded(Transportation existing, Transportation updated) {
        if (!existing.getFromStorage().getId().equals(updated.getFromStorage().getId())) {
            return storageService.getById(updated.getFromStorage().getId())
                    .switchIfEmpty(Mono.error(new StorageNotFoundException("From storage not found with ID: " + updated.getFromStorage().getId())))
                    .doOnNext(existing::setFromStorage)
                    .then();
        }
        return Mono.empty();
    }

    private Mono<Void> updateToStorageIfNeeded(Transportation existing, Transportation updated) {
        if (!existing.getToStorage().getId().equals(updated.getToStorage().getId())) {
            return storageService.getById(updated.getToStorage().getId())
                    .switchIfEmpty(Mono.error(new StorageNotFoundException("To storage not found with ID: " + updated.getToStorage().getId())))
                    .doOnNext(existing::setToStorage)
                    .then();
        }
        return Mono.empty();
    }

    private Mono<Void> checkAvailability(Long driverId, Long vehicleId, LocalDateTime start, LocalDateTime end) {
        // Заглушка. Реализация зависит от вашей логики.
        // Если хотите — могу добавить вызов реактивного клиента или локальную валидацию через JPA (в boundedElastic).
        return Mono.empty();
    }

    private boolean isFinalStatus(TransportStatus status) {
        return status == TransportStatus.DELIVERED || status == TransportStatus.CANCELLED;
    }
}