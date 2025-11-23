package com.example.warehouse.service.interfaces;

import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface UserService {
    Mono<User> createUser(User user);
    Mono<User> getUserById(Long id);
    Mono<User> getUserByEmail(String email);
    Flux<User> getAllUsers();
    Flux<User> getUsersByRole(RoleType role);
    Flux<User> searchUsersByLastName(String lastName);
    Mono<User> updateUser(Long id, User user);
    Mono<Void> deleteUser(Long id);
    Mono<Boolean> existsByEmail(String email);
    Flux<User> getUsersCreatedBetween(LocalDateTime start, LocalDateTime end);
    Mono<Long> countUsersByRole(RoleType role);
}