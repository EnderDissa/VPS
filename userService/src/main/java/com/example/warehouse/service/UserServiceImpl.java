package com.example.warehouse.service;

import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.mapper.UserMapper;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Mono<User> createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        return existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists"));
                    }
                    user.setCreatedAt(LocalDateTime.now());
                    return Mono.fromCallable(() -> userRepository.save(user))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(savedUser -> log.info("User created successfully with ID: {}", savedUser.getId()));
    }

    @Override
    public Mono<User> getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        return Mono.fromCallable(() -> userRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new UserNotFoundException("User not found with ID: " + id))));
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        return Mono.fromCallable(() -> userRepository.findByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.error(new UserNotFoundException("User not found with email: " + email))));
    }

    @Override
    public Flux<User> getAllUsers() {
        log.debug("Fetching all users");

        return Flux.fromIterable(userRepository.findAll())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<User> getUsersByRole(RoleType role) {
        log.debug("Fetching users by role: {}", role);

        return Flux.fromIterable(userRepository.findByRole(role))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<User> searchUsersByLastName(String lastName) {
        log.debug("Searching users by last name: {}", lastName);

        return Flux.fromIterable(userRepository.findByLastNameContainingIgnoreCase(lastName))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<User> updateUser(Long id, User user) {
        log.info("Updating user with ID: {}", id);

        return Mono.fromCallable(() -> userRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Mono.error(new UserNotFoundException("User not found with ID: " + id));
                    }

                    User existingUser = optional.get();

                    return existsByEmail(user.getEmail())
                            .flatMap(exists -> {
                                if (!existingUser.getEmail().equals(user.getEmail()) && exists) {
                                    return Mono.error(new UserAlreadyExistsException("Email " + user.getEmail() + " is already taken"));
                                }

                                return Mono.fromCallable(() -> userRepository.save(user))
                                        .subscribeOn(Schedulers.boundedElastic());
                            });
                })
                .doOnSuccess(updatedUser -> log.info("User with ID: {} updated successfully", id));
    }

    @Override
    public Mono<Void> deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        return Mono.fromCallable(() -> userRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new UserNotFoundException("User not found with ID: " + id));
                    }

                    return Mono.fromCallable(() -> {
                        userRepository.deleteById(id);
                        return null;
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(v -> log.info("User with ID: {} deleted successfully", id))
                .then();
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return Mono.fromCallable(() -> userRepository.existsByEmail(email))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<User> getUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching users created between {} and {}", start, end);

        return Flux.fromIterable(userRepository.findUsersCreatedBetween(start, end))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> countUsersByRole(RoleType role) {
        log.debug("Counting users with role: {}", role);

        return Mono.fromCallable(() -> (long) userRepository.findByRole(role).size())
                .subscribeOn(Schedulers.boundedElastic());
    }
}