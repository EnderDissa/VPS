package com.example.warehouse.controller;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.enumeration.RoleType;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final UserService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public UserResponseDTO getById(@PathVariable Long id) {
        return service.getUserById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by id")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UserRequestDTO dto) {
        service.updateUser(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

//     @GetMapping
//     @Operation(summary = "List users with pagination and total count")
//     public ResponseEntity<List<UserResponseDTO>> list(
//             @RequestParam(defaultValue = "0") @Min(0) int page,
//             @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
//             @RequestParam(required = false) RoleType role,
//             @RequestParam(required = false) String emailLike
//     ) {
//         var result = service.findPage(page, size, role, emailLike);
//         var headers = new HttpHeaders();
//         headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
//         return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
//     }
}
