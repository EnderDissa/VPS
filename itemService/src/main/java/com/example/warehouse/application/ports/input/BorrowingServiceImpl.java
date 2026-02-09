package com.example.warehouse.application.ports.input;

import com.example.warehouse.infrastructure.client.UserServiceClient;
import com.example.warehouse.infrastructure.web.dto.BorrowingDTO;
import com.example.warehouse.infrastructure.persistence.entity.Borrowing;
import com.example.warehouse.infrastructure.persistence.entity.Item;
import com.example.warehouse.domain.enumeration.BorrowStatus;
import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.infrastructure.web.mapper.BorrowingMapper;
import com.example.warehouse.application.ports.output.BorrowingRepository;
import com.example.warehouse.application.ports.input.interfaces.BorrowingService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceClient userService;
    private final BorrowingMapper mapper;

    @Override
    public Mono<Borrowing> getById(Long id) {
        log.debug("Getting borrowing by id: {}", id);

        return Mono.fromCallable(() -> borrowingRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new EntityNotFoundException("Borrowing not found with id: " + id))));
    }

    @Override
    public Mono<Borrowing> create(BorrowingDTO dto) {
        Borrowing entity = mapper.toEntity(dto);

        log.debug("Creating new borrowing: {}", entity);

        return itemService.getById(entity.getItem().getId())
                .flatMap(item -> ReactiveSecurityContextHolder
                        .getContext()
                        .flatMap(context ->
                        userService.getUserById(entity.getUserId(), (String) context.getAuthentication().getCredentials())
                        .flatMap(user -> {
                            if (item.getCondition() == ItemCondition.NEEDS_MAINTENANCE ||
                                    item.getCondition() == ItemCondition.UNDER_REPAIR ||
                                    item.getCondition() == ItemCondition.DECOMMISSIONED) {
                                return Mono.error(new IllegalStateException("Cannot borrow item in condition: " + item.getCondition()));
                            }

                            return Mono.fromCallable(() -> borrowingRepository.countActiveBorrowingsByUser(user))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .flatMap(activeCount -> {
                                        if (activeCount >= 5) {
                                            return Mono.error(new IllegalStateException("User has reached maximum active borrowings limit (5)"));
                                        }

                                        entity.setId(null);
                                        entity.setItem(item);
                                        entity.setUserId(user);
                                        entity.setStatus(BorrowStatus.ACTIVE);

                                        return Mono.fromCallable(() -> borrowingRepository.save(entity))
                                                .subscribeOn(Schedulers.boundedElastic());
                                    });
                        })));
    }

    @Override
    public Mono<Void> activate(Long id) {
        return Mono.fromCallable(() -> borrowingRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("Borrowing not found with id: " + id));
                    }

                    Borrowing borrowing = optional.get();
                    Item item = borrowing.getItem();

                    if (item.getCondition() == ItemCondition.UNDER_REPAIR ||
                            item.getCondition() == ItemCondition.NEEDS_MAINTENANCE ||
                            item.getCondition() == ItemCondition.DECOMMISSIONED) {
                        return Mono.error(new IllegalStateException("Cannot activate borrowing - item is not available: " + item.getCondition()));
                    }

                    borrowing.setStatus(BorrowStatus.ACTIVE);
                    borrowing.setBorrowDate(LocalDateTime.now());

                    return Mono.fromCallable(() -> borrowingRepository.save(borrowing))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Borrowing activated successfully with id: {}", id))
                .then();
    }

    @Override
    public Mono<Borrowing> extend(Long id, LocalDateTime newDueAt) {
        log.debug("Extending borrowing with id: {} to new due date: {}", id, newDueAt);

        if (newDueAt.isBefore(LocalDateTime.now())) {
            return Mono.error(new IllegalArgumentException("New due date must be in the future"));
        }

        return Mono.fromCallable(() -> borrowingRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("Borrowing not found with id: " + id));
                    }

                    Borrowing borrowing = optional.get();

                    if (borrowing.getStatus() != BorrowStatus.ACTIVE) {
                        return Mono.error(new IllegalStateException("Only active borrowings can be extended"));
                    }

                    if (newDueAt.isBefore(borrowing.getExpectedReturnDate())) {
                        return Mono.error(new IllegalArgumentException("New due date must be after current expected return date"));
                    }

                    borrowing.setExpectedReturnDate(newDueAt);

                    return Mono.fromCallable(() -> borrowingRepository.save(borrowing))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(updated -> log.info("Borrowing extended successfully with id: {}, new due date: {}", id, newDueAt));
    }

    @Override
    public Mono<Borrowing> returnBorrowing(Long id) {
        log.debug("Returning borrowing with id: {}", id);

        return Mono.fromCallable(() -> borrowingRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("Borrowing not found with id: " + id));
                    }

                    Borrowing borrowing = optional.get();

                    if (borrowing.getStatus() != BorrowStatus.ACTIVE && borrowing.getStatus() != BorrowStatus.OVERDUE) {
                        return Mono.error(new IllegalStateException("Only active or overdue borrowings can be returned"));
                    }

                    borrowing.setStatus(BorrowStatus.RETURNED);
                    borrowing.setActualReturnDate(LocalDateTime.now());

                    if (borrowing.getActualReturnDate().isAfter(borrowing.getExpectedReturnDate())) {
                        log.warn("Borrowing {} was returned late", id);
                    }

                    return Mono.fromCallable(() -> borrowingRepository.save(borrowing))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(returned -> log.info("Borrowing returned successfully with id: {}", id));
    }

    @Override
    public Mono<Void> cancel(Long id) {
        log.debug("Canceling borrowing with id: {}", id);

        return Mono.fromCallable(() -> borrowingRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("Borrowing not found with id: " + id));
                    }

                    Borrowing borrowing = optional.get();

                    if (borrowing.getStatus() != BorrowStatus.ACTIVE) {
                        return Mono.error(new IllegalStateException("Only pending borrowings can be canceled"));
                    }

                    borrowing.setStatus(BorrowStatus.CANCELLED);

                    return Mono.fromCallable(() -> borrowingRepository.save(borrowing))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("Borrowing cancelled successfully with id: {}", id))
                .then();
    }


    @Override
    public Flux<Borrowing> findBorrowingsByFilters(BorrowStatus status, Long userId, Long itemId,
                                                   LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.debug("Finding borrowings with filters - pageable: {}, status: {}, userId: {}, itemId: {}, from: {}, to: {}",
                pageable, status, userId, itemId, from, to);


        Specification<Borrowing> spec = Specification.unrestricted();

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        if (itemId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("item").get("id"), itemId));
        }

        if (from != null && to != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("borrowDate"), from, to));
        } else if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("borrowDate"), from));
        } else if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("borrowDate"), to));
        }


        Specification<Borrowing> finalSpec = spec;
        return Mono.fromCallable(() -> borrowingRepository.findAll(finalSpec, pageable))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    @Override
    public Mono<Long> countBorrowingsByFilters(BorrowStatus status, Long userId, Long itemId,
                                               LocalDateTime from, LocalDateTime to) {
        log.debug("Counting borrowings with filters - status: {}, userId: {}, itemId: {}, from: {}, to: {}",
                status, userId, itemId, from, to);


        Specification<Borrowing> spec = Specification.unrestricted();

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        if (itemId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("item").get("id"), itemId));
        }

        if (from != null && to != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("borrowDate"), from, to));
        } else if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("borrowDate"), from));
        } else if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("borrowDate"), to));
        }


        Specification<Borrowing> finalSpec = spec;
        return Mono.fromCallable(() -> borrowingRepository.count(finalSpec))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Borrowing> findOverdueBorrowings(Pageable pageable) {
        log.debug("Finding overdue borrowings - pageable: {}", pageable);

        LocalDateTime now = LocalDateTime.now();


        return Mono.fromCallable(() -> borrowingRepository.findOverdueBorrowings(now, pageable))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> {
                    List<Borrowing> borrowings = page.getContent();


                    List<Borrowing> borrowingsToUpdate = borrowings.stream()
                            .filter(b -> b.getStatus() == BorrowStatus.ACTIVE)
                            .toList();

                    if (!borrowingsToUpdate.isEmpty()) {
                        borrowingsToUpdate.forEach(b -> b.setStatus(BorrowStatus.OVERDUE));

                        Mono.fromCallable(() -> borrowingRepository.saveAll(borrowingsToUpdate))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
                        log.info("Updated {} borrowings to OVERDUE status", borrowingsToUpdate.size());
                    }

                    return Flux.fromIterable(borrowings);
                });
    }

    @Override
    public Mono<Long> countOverdueBorrowings() {
        log.debug("Counting overdue borrowings");

        LocalDateTime now = LocalDateTime.now();


        return Mono.fromCallable(() -> borrowingRepository.countOverdueBorrowings(now))
                .subscribeOn(Schedulers.boundedElastic());
    }


    @Scheduled(cron = "0 0 6 * * ?")
    public void updateOverdueBorrowings() {
        log.debug("Running scheduled task to update overdue borrowings");
        LocalDateTime now = LocalDateTime.now();


        List<Borrowing> activeBorrowings = borrowingRepository.findAll()
                .stream()
                .filter(b -> b.getStatus() == BorrowStatus.ACTIVE
                        && b.getExpectedReturnDate().isBefore(now))
                .toList();

        if (!activeBorrowings.isEmpty()) {
            activeBorrowings.forEach(b -> b.setStatus(BorrowStatus.OVERDUE));
            borrowingRepository.saveAll(activeBorrowings);
            log.info("Updated {} borrowings to OVERDUE status via scheduled task", activeBorrowings.size());
        }
    }
}