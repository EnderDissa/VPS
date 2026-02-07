package com.example.warehouse.infrastructure.auth;

import com.example.warehouse.infrastructure.auth.jwt.JWTUtils;
import com.example.warehouse.infrastructure.web.dto.UserDTO.AuthRequestDTO;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.application.input.interfaces.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;

@RestController
public class AuthController {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserService userService;

    @PostMapping("/v1/users/login")
    public Mono<ResponseEntity<String>> login(@RequestBody AuthRequestDTO dto) {
        return userService.loginUser(dto.email(), dto.password())
                .doOnNext(match -> System.out.println("Auth result: " + match)) // Debug
                .flatMap(match -> {
                    if (match != null) {
                        try {
                            String token = jwtUtils.generateTokenFromUsername(match.getId().toString());
                            System.out.println("Generated token: " + token.substring(0, 10) + "..."); // Debug truncated token
                            return Mono.just(ResponseEntity.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body("{\"token\":\"" + token + "\"}"));
                        } catch (Exception e) {
                            System.err.println("Token generation failed: " + e.getMessage());
                            return Mono.just(ResponseEntity.status(500)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body("{\"error\":\"Token generation failed\"}"));
                        }
                    } else {
                        System.out.println("Invalid credentials for: " + dto.email()); // Debug
                        return Mono.just(ResponseEntity.status(401)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":\"Invalid credentials\"}"));
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Auth error: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"error\":\"Authentication service error: " + e.getMessage() + "\"}"));
                });
    }


    @PostMapping("/internal/validate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<UserDetailsEntity> validate(@RequestBody String token) {
        if (!jwtUtils.validateJwtToken(token)) {
            return Mono.error(new AccessDeniedException("Not valid"));
        }
        String username = jwtUtils.getUserNameFromJwtToken(token);

        return userService.getUserById(Long.parseLong(username))
                .flatMap(user -> {
                    if (user == null) {
                        return Mono.error(new UserNotFoundException("Not found"));
                    }

                    UserDetailsEntity ent = new UserDetailsEntity();
                    ent.setUsername(user.getFirstName());
                    ent.setAuthorities(new String[]{"ROLE_" + user.getRole().name()});
                    ent.setAccountNonExpired(true);
                    ent.setAccountNonLocked(true);
                    ent.setCredentialsNonExpired(true);
                    ent.setEnabled(true);

                    System.out.println("Auth user: " + ent);

                    return Mono.just(ent);
                });
    }
}