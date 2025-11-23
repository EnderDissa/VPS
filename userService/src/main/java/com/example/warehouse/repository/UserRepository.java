package com.example.warehouse.repository;

import com.example.warehouse.entity.User;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveSortingRepository<User, Long>, ReactiveCrudRepository<User, Long> {


    @Query("SELECT * FROM users WHERE email = :email")
    Mono<User> findByEmail(String email);


    @Query("SELECT * FROM users WHERE role = :role")
    Flux<User> findByRole(String role);


    @Query("SELECT * FROM users WHERE LOWER(last_name) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Flux<User> findByLastNameContainingIgnoreCase(String lastName);


    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    Mono<Boolean> existsByEmail(String email);


    @Query("SELECT * FROM users WHERE created_at BETWEEN :start AND :end ORDER BY created_at ASC")
    Flux<User> findUsersCreatedBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);


    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    Mono<Long> countByRole(String role);


    @Query("SELECT COUNT(*) FROM users")
    Mono<Long> count();
}