package com.example.warehouse.service;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.BorrowStatus;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.mapper.BorrowingMapper;
import com.example.warehouse.repository.BorrowingRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.service.interfaces.BorrowingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BorrowingMapper borrowingMapper;

    @Override
    @Transactional(readOnly = true)
    public BorrowingDTO getById(Long id) {
        log.debug("Getting borrowing by id: {}", id);
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));
        return borrowingMapper.toDTO(borrowing);
    }

    @Override
    public BorrowingDTO create(BorrowingDTO dto) {
        log.debug("Creating new borrowing: {}", dto);

        // Validate and fetch related entities
        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + dto.itemId()));

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.userId()));

        // Validate item condition
        if (item.getCondition() == ItemCondition.NEEDS_MAINTENANCE || item.getCondition() == ItemCondition.UNDER_REPAIR || item.getCondition() == ItemCondition.DECOMMISSIONED) {
            throw new IllegalStateException("Cannot borrow item in condition: " + item.getCondition());
        }

        // Check if user has active borrowings limit (example: max 5 active borrowings)
        long activeBorrowingsCount = borrowingRepository.countActiveBorrowingsByUser(user.getId());
        if (activeBorrowingsCount >= 5) {
            throw new IllegalStateException("User has reached maximum active borrowings limit (5)");
        }

        // Create borrowing entity
        Borrowing borrowing = borrowingMapper.toEntity(dto);
        borrowing.setItem(item);
        borrowing.setUser(user);
        borrowing.setStatus(BorrowStatus.ACTIVE);

        Borrowing savedBorrowing = borrowingRepository.save(borrowing);
        log.info("Borrowing created successfully with id: {}", savedBorrowing.getId());

        return borrowingMapper.toDTO(savedBorrowing);
    }

    @Override
    public void activate(Long id) {
        log.debug("Activating borrowing with id: {}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));

        if (borrowing.getStatus() != BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Only pending borrowings can be activated");
        }

        // Check item availability
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
    public BorrowingDTO extend(Long id, LocalDateTime newDueAt) {
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
        return borrowingMapper.toDTO(updatedBorrowing);
    }

    @Override
    public BorrowingDTO returnBorrowing(Long id) {
        log.debug("Returning borrowing with id: {}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with id: " + id));

        if (borrowing.getStatus() != BorrowStatus.ACTIVE && borrowing.getStatus() != BorrowStatus.OVERDUE) {
            throw new IllegalStateException("Only active or overdue borrowings can be returned");
        }

        borrowing.setStatus(BorrowStatus.RETURNED);
        borrowing.setActualReturnDate(LocalDateTime.now());

        // Check if returned late
        if (borrowing.getActualReturnDate().isAfter(borrowing.getExpectedReturnDate())) {
            log.warn("Borrowing {} was returned late", id);
            // Here you could add late fee logic or notifications
        }

        Borrowing returnedBorrowing = borrowingRepository.save(borrowing);
        log.info("Borrowing returned successfully with id: {}", id);

        return borrowingMapper.toDTO(returnedBorrowing);
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
    @Transactional(readOnly = true)
    public Page<BorrowingDTO> findPage(int page, int size, BorrowStatus status, Long userId, Long itemId,
                                       LocalDateTime from, LocalDateTime to) {
        log.debug("Finding borrowings with filters - page: {}, size: {}, status: {}, userId: {}, itemId: {}, from: {}, to: {}",
                page, size, status, userId, itemId, from, to);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowDate"));

        // Исправлено: Specification.where(null) вместо unrestricted()
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

        // Теперь этот метод будет доступен
        Page<Borrowing> borrowings = borrowingRepository.findAll(spec, pageable);
        return borrowings.map(borrowingMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingDTO> findOverdue(int page, int size) {
        log.debug("Finding overdue borrowings - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "expectedReturnDate"));
        LocalDateTime now = LocalDateTime.now();

        Page<Borrowing> overdueBorrowings = borrowingRepository.findOverdueBorrowings(now, pageable);

        // Update status to OVERDUE for found borrowings
        List<Borrowing> borrowingsToUpdate = overdueBorrowings.getContent().stream()
                .filter(b -> b.getStatus() == BorrowStatus.ACTIVE)
                .toList();

        if (!borrowingsToUpdate.isEmpty()) {
            borrowingsToUpdate.forEach(b -> b.setStatus(BorrowStatus.OVERDUE));
            borrowingRepository.saveAll(borrowingsToUpdate);
            log.info("Updated {} borrowings to OVERDUE status", borrowingsToUpdate.size());
        }

        return overdueBorrowings.map(borrowingMapper::toDTO);
    }

    // Additional helper method for automatic status updates
    @Scheduled(cron = "0 0 6 * * ?") // Run daily at 6 AM
    @Transactional
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