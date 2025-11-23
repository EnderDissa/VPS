package com.example.warehouse.controller;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.mapper.UserMapper;
import com.example.warehouse.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final UserService service;
    private final UserMapper mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UserController(UserService service, UserMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Create user")
    public Mono<ResponseEntity<UserResponseDTO>> create(@Valid @RequestBody UserRequestDTO dto) {
        return service.createUser(mapper.toEntity(dto))
                .map(mapper::toResponseDTO)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public Mono<UserResponseDTO> getById(@PathVariable Long id) {
        return service.getUserById(id)
                .map(mapper::toResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by id")
    public Mono<ResponseEntity<Void>> update(@PathVariable Long id, @Valid @RequestBody UserRequestDTO dto) {
        return service.updateUser(id, mapper.toEntity(dto))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.deleteUser(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    // Additional endpoints that were in the original service
    @GetMapping
    @Operation(summary = "Get all users")
    public Flux<UserResponseDTO> getAll() {
        return service.getAllUsers()
                .map(mapper::toResponseDTO);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role")
    public Flux<UserResponseDTO> getByRole(@PathVariable RoleType role) {
        return service.getUsersByRole(role)
                .map(mapper::toResponseDTO);
    }

    @GetMapping("/search/lastName/{lastName}")
    @Operation(summary = "Search users by last name")
    public Flux<UserResponseDTO> searchByLastName(@PathVariable String lastName) {
        return service.searchUsersByLastName(lastName)
                .map(mapper::toResponseDTO);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public Mono<UserResponseDTO> getByEmail(@PathVariable String email) {
        return service.getUserByEmail(email)
                .map(mapper::toResponseDTO);
    }

    @GetMapping("/exists/{email}")
    @Operation(summary = "Check if user exists by email")
    public Mono<Boolean> existsByEmail(@PathVariable String email) {
        return service.existsByEmail(email);
    }

    @GetMapping("/created-between")
    @Operation(summary = "Get users created between dates")
    public Flux<UserResponseDTO> getUsersCreatedBetween(
            @RequestParam("start") LocalDateTime start,
            @RequestParam("end") LocalDateTime end
    ) {
        return service.getUsersCreatedBetween(start, end)
                .map(mapper::toResponseDTO);
    }

    @GetMapping("/count/role/{role}")
    @Operation(summary = "Count users by role")
    public Mono<Long> countByRole(@PathVariable RoleType role) {
        return service.countUsersByRole(role);
    }
}