package com.example.warehouse.auth;


import com.example.warehouse.auth.jwt.JWTAuthFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;



@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Autowired
    CrossServiceUserRepository userRepository = null;

    @Autowired
    JWTAuthFilter jwtFilter = null;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth ->
                        auth
                                .pathMatchers("/v3/api-docs")
                                .permitAll()
                                .pathMatchers("/api/v1/storages", "/api/v1/storages/**")
                                .hasAnyRole("MANAGER", "ADMIN")
                                .pathMatchers("/api/v1/transportations", "/api/v1/transportations/**")
                                .hasAnyRole("DRIVER","ADMIN")
                                .pathMatchers("/api/v1/vehicles", "/api/v1/vehicles/**")
                                .hasAnyRole("DRIVER","ADMIN")
                                .anyExchange()
                                .hasRole("ADMIN")
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.LOGOUT)
            .build();
    }

    @Bean
    AuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(this.userRepository.getUserDetailsService());
        provider.setUserDetailsPasswordService(this.userRepository.getUserDetailsPasswordService());
        return provider;
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return this.userRepository.getUserDetailsService();
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}