package com.example.warehouse.auth;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mastik.gateway.auth.jwt.JWTUtils;

import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
public class AuthController {

    @Autowired
    private ServiceUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtils jwtUtils;

    @Value
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ApiSuccessResponse {
        int success = 1;
        Object result;

        public ApiSuccessResponse(Object result) {
            this.result = result;
        }
    }

    @Value
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ApiErrorResponse {
        int success = 0;
        String error;

        public ApiErrorResponse(String error) {
            this.error = error;
        }
    }

    @Value
    static class LoginResult {
        String user;
        String token;
        long expires;

        public LoginResult(String user, String token, long expires) {
            this.user = user;
            this.token = token;
            this.expires = expires;
        }
    }

    @PostMapping("/internal/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<ResponseEntity<Object>> login(@RequestBody UserRequestDTO user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.email(),
                user.password()
        );
        String error = "user not exists";
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();

        try {
            auth = authenticationManager.authenticate(auth);
            if (auth != null && auth.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
                return Mono.just(response.body(
                        new ApiSuccessResponse(new LoginResult(
                                user.email(),
                                jwtCookie.toString(),
                                Instant.now().plusMillis(jwtUtils.getJwtExpirationMs()).toEpochMilli()
                        ))
                ));
            }
        } catch (BadCredentialsException e) {
            error = "invalid password";
        }

        return Mono.just(response.body(new ApiErrorResponse(error)));
    }
}