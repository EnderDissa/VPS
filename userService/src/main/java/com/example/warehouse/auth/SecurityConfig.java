package com.example.warehouse.auth;

import com.mastik.gateway.auth.jwt.JWTAuthFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    ServiceUserRepository userRepository = null;

    @Autowired
    JWTAuthFilter jwtFilter = null;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth ->
                        auth
                                .pathMatchers("/internal/login")
                                .permitAll()
                                .anyExchange()
                                .authenticated()
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.LOGOUT)
            .build();
    }

    @Bean
    AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(this.userRepository.getUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
//        provider.setUserDetailsPasswordService(this.userRepository.getUserDetailsPasswordService());
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