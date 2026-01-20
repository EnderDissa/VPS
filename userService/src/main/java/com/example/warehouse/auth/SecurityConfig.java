package com.example.warehouse.auth;

import com.example.warehouse.auth.jwt.JWTAuthFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Autowired
    JWTAuthFilter jwtFilter;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth ->
                        auth
                                .pathMatchers("/v3/api-docs")
                                .permitAll()
                                .pathMatchers(HttpMethod.POST, "/v1/users/login")
                                .permitAll()
                                .pathMatchers(HttpMethod.POST, "/internal/validate")
                                .permitAll()
                                .pathMatchers("/api/v1/users", "/api/v1/users/**")
                                .hasAnyRole("ADMIN")
                                .pathMatchers("/api/v1/user-storage-access", "/api/v1/user-storage-access/**")
                                .hasAnyRole("MANAGER","ADMIN")
                                .anyExchange()
                                .hasRole("ADMIN")
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.LOGOUT)
            .build();
    }

}