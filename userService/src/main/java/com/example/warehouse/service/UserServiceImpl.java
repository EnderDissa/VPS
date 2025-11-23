package com.example.warehouse.service;

import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        return existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists"));
                    }
                    user.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .doOnSuccess(savedUser -> log.info("User created successfully with ID: {}", savedUser.getId()));
    }

    @Override
    public Mono<User> getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with ID: " + id)));
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)));
    }

    @Override
    public Flux<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public Flux<User> getUsersByRole(String role) {
        log.debug("Fetching users by role: {}", role);
        return userRepository.findByRole(role);
    }

    @Override
    public Flux<User> searchUsersByLastName(String lastName) {
        log.debug("Searching users by last name: {}", lastName);
        return userRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    @Override
    public Mono<User> updateUser(Long id, User user) {
        log.info("Updating user with ID: {}", id);

        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with ID: " + id)))
                .flatMap(existingUser -> {
                    if (!existingUser.getEmail().equals(user.getEmail())) {
                        return existsByEmail(user.getEmail())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new UserAlreadyExistsException("Email " + user.getEmail() + " is already taken"));
                                    }

                                    user.setCreatedAt(existingUser.getCreatedAt());
                                    return userRepository.save(user);
                                });
                    } else {

                        user.setCreatedAt(existingUser.getCreatedAt());
                        return userRepository.save(user);
                    }
                })
                .doOnSuccess(updatedUser -> log.info("User with ID: {} updated successfully", id));
    }

    @Override
    public Mono<Void> deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        return userRepository.deleteById(id)
                .doOnSuccess(v -> log.info("User with ID: {} deleted successfully", id));
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Flux<User> getUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching users created between {} and {}", start, end);
        return userRepository.findUsersCreatedBetween(start, end);
    }

    @Override
    public Mono<Long> countUsersByRole(String role) {
        log.debug("Counting users with role: {}", role);
        return userRepository.countByRole(role);
    }
}