package com.example.warehouse.application.ports.input;

import com.example.warehouse.infrastructure.client.UserServiceClient;
import com.example.warehouse.infrastructure.persistence.entity.Item;
import com.example.warehouse.infrastructure.persistence.entity.ItemMaintenance;
import com.example.warehouse.infrastructure.persistence.entity.User;
import com.example.warehouse.domain.enumeration.MaintenanceStatus;
import com.example.warehouse.exception.ItemMaintenanceNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.application.ports.output.ItemMaintenanceRepository;
import com.example.warehouse.application.ports.input.interfaces.ItemMaintenanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemMaintenanceServiceImpl implements ItemMaintenanceService {

    private final ItemMaintenanceRepository itemMaintenanceRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceClient userService;

    @Override
    public Mono<ItemMaintenance> create(ItemMaintenance maintenance) {
        log.info("Creating new item maintenance for item ID: {}", maintenance.getItem().getId());

        return itemService.getById(maintenance.getItem().getId())
                .flatMap(item -> userService.getUserById(maintenance.getTechnicianId())
                        .map(technician -> {
                            maintenance.setItem(item);
                            maintenance.setTechnicianId(technician.getId());
                            return maintenance;
                        }))
                .flatMap(maint -> Mono.fromCallable(() -> itemMaintenanceRepository.save(maint))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnSuccess(savedMaintenance -> log.info("Item maintenance created successfully with ID: {}", savedMaintenance.getId()));
    }

    @Override
    public Mono<ItemMaintenance> getById(Long id) {
        log.debug("Fetching item maintenance by ID: {}", id);

        return Mono.fromCallable(() -> itemMaintenanceRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id))));
    }

    @Override
    public Mono<Void> update(Long id, ItemMaintenance maintenance) {
        return Mono.fromCallable(() -> itemMaintenanceRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));
                    }

                    ItemMaintenance existingMaintenance = optional.get();

                    Mono<Item> itemMono = Mono.just(maintenance.getItem().getId())
                            .filter(itemId -> !existingMaintenance.getItem().getId().equals(itemId))
                            .flatMap(itemService::getById)
                            .switchIfEmpty(Mono.just(existingMaintenance.getItem()))
                            .doOnNext(existingMaintenance::setItem);

                    Mono<User> technicianMono = Mono.just(maintenance.getTechnicianId())
                            .filter(techId -> !existingMaintenance.getTechnicianId().equals(techId))
                            .flatMap( technicianId -> userService.getUserById(technicianId)
                                    .switchIfEmpty(Mono.error(new UserNotFoundException(maintenance.getTechnicianId()))))
                            .doOnNext(tech -> existingMaintenance.setTechnicianId(tech.getId()));

                    return itemMono.then(technicianMono)
                            .doOnNext(v -> {
                                existingMaintenance.setMaintenanceDate(maintenance.getMaintenanceDate());
                                existingMaintenance.setNextMaintenanceDate(maintenance.getNextMaintenanceDate());
                                existingMaintenance.setCost(maintenance.getCost());
                                existingMaintenance.setDescription(maintenance.getDescription());
                                existingMaintenance.setStatus(maintenance.getStatus());
                            })
                            .then(Mono.fromCallable(() -> itemMaintenanceRepository.save(existingMaintenance))
                                    .subscribeOn(Schedulers.boundedElastic()));
                })
                .doOnSuccess(v -> log.info("Item maintenance with ID: {} updated successfully", id))
                .then();
    }

    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromCallable(() -> itemMaintenanceRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));
                    }
                    return Mono.fromCallable(() -> {
                        itemMaintenanceRepository.deleteById(id);
                        return null;
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Item maintenance with ID: {} deleted successfully", id))
                .then();
    }


    @Override
    public Flux<ItemMaintenance> findMaintenancesByFilters(Long itemId, MaintenanceStatus status, Pageable pageable) {
        log.debug("Fetching item maintenance page - pageable: {}, itemId: {}, status: {}",
                pageable, itemId, status);

        return Mono.fromCallable(() -> {
                    if (itemId != null && status != null) {
                        return itemMaintenanceRepository.findByItemIdAndStatus(itemId, status, pageable);
                    } else if (itemId != null) {
                        return itemMaintenanceRepository.findByItemId(itemId, pageable);
                    } else if (status != null) {
                        return itemMaintenanceRepository.findByStatus(status, pageable);
                    } else {
                        return itemMaintenanceRepository.findAll(pageable);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    @Override
    public Mono<Long> countMaintenancesByFilters(Long itemId, MaintenanceStatus status) {
        log.debug("Counting item maintenance records - itemId: {}, status: {}", itemId, status);

        return Mono.fromCallable(() -> {
                    if (itemId != null && status != null) {
                        return itemMaintenanceRepository.countByItemIdAndStatus(itemId, status);
                    } else if (itemId != null) {
                        return itemMaintenanceRepository.countByItemId(itemId);
                    } else if (status != null) {
                        return itemMaintenanceRepository.countByStatus(status);
                    } else {
                        return itemMaintenanceRepository.count();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }


    @Override
    public Flux<ItemMaintenance> findByTechnician(Long technicianId, Pageable pageable) {
        log.debug("Fetching item maintenance by technician ID: {}", technicianId);

        return Mono.fromCallable(() -> {
                    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "maintenanceDate"));
                    return itemMaintenanceRepository.findByTechnicianId(technicianId, pageRequest);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    @Override
    public Mono<Long> countByStatus(MaintenanceStatus status) {
        log.debug("Counting item maintenance records with status: {}", status);
        return Mono.fromCallable(() -> itemMaintenanceRepository.countByStatus(status))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> updateStatus(Long id, MaintenanceStatus status) {
        log.info("Updating status to {} for item maintenance ID: {}", status, id);

        return Mono.fromCallable(() -> itemMaintenanceRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new ItemMaintenanceNotFoundException("Item maintenance not found with ID: " + id));
                    }

                    ItemMaintenance maintenance = optional.get();
                    maintenance.setStatus(status);

                    return Mono.fromCallable(() -> itemMaintenanceRepository.save(maintenance))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Status updated successfully for item maintenance ID: {}", id))
                .then();
    }
}