package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> createUser(User user);
    Mono<User> getUserById(Long id);
    Mono<User> getUserByEmail(String email);
    Flux<User> getAllUsers();
    Flux<User> getUsersByRole(String role); // Use String
    Flux<User> searchUsersByLastName(String lastName);
    Mono<User> updateUser(Long id, User user);
    Mono<Void> deleteUser(Long id);
    Mono<Boolean> existsByEmail(String email);
    Flux<User> getUsersCreatedBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    Mono<Long> countUsersByRole(String role); // Use String
}