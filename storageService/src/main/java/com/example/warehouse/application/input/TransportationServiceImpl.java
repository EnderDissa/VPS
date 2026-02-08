package com.example.warehouse.application.input;

import com.example.warehouse.domain.exception.*;
import com.example.warehouse.infrastructure.client.ItemServiceClient;
import com.example.warehouse.infrastructure.client.UserServiceClient;
import com.example.warehouse.domain.enumeration.TransportStatus;
import com.example.warehouse.infrastructure.persistence.entity.*;
import com.example.warehouse.application.output.TransportationRepository;
import com.example.warehouse.application.input.interfaces.StorageService;
import com.example.warehouse.application.input.interfaces.TransportationService;
import com.example.warehouse.application.input.interfaces.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

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
                transportation.getItemId(),
                transportation.getFromStorage().getId(),
                transportation.getToStorage().getId());

        // Проверяем, что склады разные
        if (transportation.getFromStorage().getId().equals(transportation.getToStorage().getId())) {
            return Mono.error(new OperationNotAllowedException("From and to storage cannot be the same"));
        }

        // Получаем связанные сущности через клиенты (они уже реактивные)
        return Mono.zip(
                        itemServiceClient.getItemById(transportation.getItemId())
                                .switchIfEmpty(Mono.error(new ItemNotFoundException("Item not found with ID: " + transportation.getItemId()))),
                        ReactiveSecurityContextHolder
                                .getContext()
                                .flatMap(context ->
                                        userServiceClient.getUserById(transportation.getDriverId(), (String) context.getAuthentication().getCredentials()))
                                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with ID: " + transportation.getDriverId()))),
                        vehicleService.getById(transportation.getVehicle().getId())
                                .switchIfEmpty(Mono.error(new VehicleNotFoundException("Vehicle not found with ID: " + transportation.getVehicle().getId()))),
                        storageService.getById(transportation.getFromStorage().getId())
                                .switchIfEmpty(Mono.error(new StorageNotFoundException("From storage not found with ID: " + transportation.getFromStorage().getId()))),
                        storageService.getById(transportation.getToStorage().getId())
                                .switchIfEmpty(Mono.error(new StorageNotFoundException("To storage not found with ID: " + transportation.getToStorage().getId())))
                )
                .flatMap(tuple -> {
                    Item item = tuple.getT1();
                    Long driver = tuple.getT2();
                    Vehicle vehicle = tuple.getT3();
                    Storage fromStorage = tuple.getT4();
                    Storage toStorage = tuple.getT5();

                    // Проверяем доступность (оставляем как реактивный вызов — клиент или локальная заглушка)
                    return checkAvailability(driver, vehicle.getId(),
                            transportation.getScheduledDeparture(), transportation.getScheduledArrival())
                            .then(Mono.fromCallable(() -> {
                                transportation.setItemId(item.getId());
                                transportation.setDriverId(driver);
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
        return updateItemIfNeeded(existing, updated)
                .then(updateDriverIfNeeded(existing, updated))
                .then(updateVehicleIfNeeded(existing, updated))
                .then(updateFromStorageIfNeeded(existing, updated))
                .then(updateToStorageIfNeeded(existing, updated))
                .thenReturn(existing);
    }

    private Mono<Void> updateItemIfNeeded(Transportation existing, Transportation updated) {
        if (!existing.getItemId().equals(updated.getItemId())) {
            return itemServiceClient.getItemById(updated.getItemId())
                    .switchIfEmpty(Mono.error(new ItemNotFoundException("Item not found with ID: " + updated.getItemId())))
                    .doOnNext(item -> existing.setItemId(item.getId()))
                    .then();
        }
        return Mono.empty();
    }

    private Mono<Void> updateDriverIfNeeded(Transportation existing, Transportation updated) {
        if (!existing.getDriverId().equals(updated.getDriverId())) {
            return ReactiveSecurityContextHolder
                    .getContext()
                    .flatMap(context -> userServiceClient.getUserById(updated.getDriverId(), (String) context.getAuthentication().getCredentials())
                    .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with ID: " + updated.getDriverId())))
                    .doOnNext(existing::setDriverId)
                    .then());
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