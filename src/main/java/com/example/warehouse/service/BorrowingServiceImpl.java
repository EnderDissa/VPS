package com.example.warehouse.service;

import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.BorrowStatus;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.repository.BorrowingRepository;
import com.example.warehouse.service.interfaces.BorrowingService;
import com.example.warehouse.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final ItemServiceImpl itemService;
    private final UserService userService;

    @Override
    public Borrowing getById(Long id) {
        log.debug("Getting borrowing by id: {}", id);
        return borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));
    }

    @Override
    public Borrowing create(Borrowing entity) {
        log.debug("Creating new borrowing: {}", entity);

        Item item = itemService.getById(entity.getItem().getId());

        User user = userService.getUserById(entity.getUser().getId());

        if (item.getCondition() == ItemCondition.NEEDS_MAINTENANCE || item.getCondition() == ItemCondition.UNDER_REPAIR || item.getCondition() == ItemCondition.DECOMMISSIONED) {
            throw new IllegalStateException("Cannot borrow item in condition: " + item.getCondition());
        }

        long activeBorrowingsCount = borrowingRepository.countActiveBorrowingsByUser(user.getId());
        if (activeBorrowingsCount >= 5) {
            throw new IllegalStateException("User has reached maximum active borrowings limit (5)");
        }

        entity.setId(null);
        entity.setItem(item);
        entity.setUser(user);
        entity.setStatus(BorrowStatus.ACTIVE);

        Borrowing savedBorrowing = borrowingRepository.save(entity);
        log.info("Borrowing created successfully with id: {}", savedBorrowing.getId());

        return savedBorrowing;
    }

    @Override
    public void activate(Long id) {
        log.debug("Activating borrowing with id: {}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));

        if (borrowing.getStatus() != BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Only pending borrowings can be activated");
        }

        Item item = borrowing.getItem();
        if (item.getCondition() == ItemCondition.UNDER_REPAIR || item.getCondition() == ItemCondition.NEEDS_MAINTENANCE || item.getCondition() == ItemCondition.DECOMMISSIONED) {
            throw new IllegalStateException("Cannot activate borrowing - item is not available: " + item.getCondition());
        }

        borrowing.setStatus(BorrowStatus.ACTIVE);
        borrowing.setBorrowDate(LocalDateTime.now());

        borrowingRepository.save(borrowing);
        log.info("Borrowing activated successfully with id: {}", id);
    }

    @Override
    public Borrowing extend(Long id, LocalDateTime newDueAt) {
        log.debug("Extending borrowing with id: {} to new due date: {}", id, newDueAt);

        if (newDueAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New due date must be in the future");
        }

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));

        if (borrowing.getStatus() != BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Only active borrowings can be extended");
        }

        if (newDueAt.isBefore(borrowing.getExpectedReturnDate())) {
            throw new IllegalArgumentException("New due date must be after current expected return date");
        }

        borrowing.setExpectedReturnDate(newDueAt);
        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);

        log.info("Borrowing extended successfully with id: {}, new due date: {}", id, newDueAt);
        return updatedBorrowing;
    }

    @Override
    public Borrowing returnBorrowing(Long id) {
        log.debug("Returning borrowing with id: {}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));

        if (borrowing.getStatus() != BorrowStatus.ACTIVE && borrowing.getStatus() != BorrowStatus.OVERDUE) {
            throw new IllegalStateException("Only active or overdue borrowings can be returned");
        }

        borrowing.setStatus(BorrowStatus.RETURNED);
        borrowing.setActualReturnDate(LocalDateTime.now());

        if (borrowing.getActualReturnDate().isAfter(borrowing.getExpectedReturnDate())) {
            log.warn("Borrowing {} was returned late", id);

        }

        Borrowing returnedBorrowing = borrowingRepository.save(borrowing);
        log.info("Borrowing returned successfully with id: {}", id);

        return returnedBorrowing;
    }

    @Override
    public void cancel(Long id) {
        log.debug("Canceling borrowing with id: {}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));

        if (borrowing.getStatus() != BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Only pending borrowings can be canceled");
        }

        borrowing.setStatus(BorrowStatus.CANCELLED);
        borrowingRepository.save(borrowing);

        log.info("Borrowing cancelled successfully with id: {}", id);
    }

    @Override
    
    public Page<Borrowing> findPage(int page, int size, BorrowStatus status, Long userId, Long itemId,
                                       LocalDateTime from, LocalDateTime to) {
        log.debug("Finding borrowings with filters - page: {}, size: {}, status: {}, userId: {}, itemId: {}, from: {}, to: {}",
                page, size, status, userId, itemId, from, to);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowDate"));

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

        return borrowingRepository.findAll(spec, pageable);
    }

    @Override
    
    public Page<Borrowing> findOverdue(int page, int size) {
        log.debug("Finding overdue borrowings - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "expectedReturnDate"));
        LocalDateTime now = LocalDateTime.now();

        Page<Borrowing> overdueBorrowings = borrowingRepository.findOverdueBorrowings(now, pageable);

        List<Borrowing> borrowingsToUpdate = overdueBorrowings.getContent().stream()
                .filter(b -> b.getStatus() == BorrowStatus.ACTIVE)
                .toList();

        if (!borrowingsToUpdate.isEmpty()) {
            borrowingsToUpdate.forEach(b -> b.setStatus(BorrowStatus.OVERDUE));
            borrowingRepository.saveAll(borrowingsToUpdate);
            log.info("Updated {} borrowings to OVERDUE status", borrowingsToUpdate.size());
        }

        return overdueBorrowings;
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