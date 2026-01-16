package com.mastik.gateway.auth.jwt;

import com.mastik.gateway.auth.CrossServiceUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class JWTAuthFilter implements WebFilter {

    private final JWTUtils jwtUtils;
    private final CrossServiceUserRepository userRepository;

    public JWTAuthFilter(JWTUtils jwtUtils, CrossServiceUserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            String jwt = parseJwt(exchange);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetailsService userDetailsService = userRepository.getUserDetailsService();

                return Mono.fromCallable(() -> userDetailsService.loadUserByUsername(username))
                        .onErrorResume(ex -> {
                            log.warn("Failed to load user details for username: {}", username, ex);
                            return Mono.empty();
                        })
                        .flatMap(userDetails -> {
                            Authentication authentication = buildAuthentication(userDetails);
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                        })
                        .onErrorResume(ex -> {
                            log.error("Authentication failed during filter processing", ex);
                            return chain.filter(exchange);
                        });
            }
        } catch (Exception e) {
            log.warn("JWT processing error: {}", e.getMessage(), e);
        }

        return chain.filter(exchange);
    }

    private String parseJwt(ServerWebExchange exchange) {
        return jwtUtils.getJwtFromCookies(exchange.getRequest());
    }

    private Authentication buildAuthentication(UserDetails userDetails) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("USER")
        );

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );
    }
}