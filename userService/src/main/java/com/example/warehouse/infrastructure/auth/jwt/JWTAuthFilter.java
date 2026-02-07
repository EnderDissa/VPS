package com.example.warehouse.infrastructure.auth.jwt;

import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import com.example.warehouse.application.input.interfaces.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JWTAuthFilter implements WebFilter {

    @Autowired
    UserService userService;

    @Autowired
    JWTUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            String jwt = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (jwt != null && jwtUtils.validateJwtToken(jwt.substring(7))) {
                jwt = jwt.substring(7);

                String finalJwt = jwt;
                return userService.getUserById(Long.parseLong(jwtUtils.getUserNameFromJwtToken(finalJwt)))
                        .onErrorResume(ex -> {
                            log.warn("Failed to load user details for username: {}", finalJwt, ex);
                            return Mono.empty();
                        })
                        .flatMap(user -> {
                            UserDetailsEntity ent = new UserDetailsEntity(user.getId().toString(), "1", new String[]{"ROLE_" + user.getRole().name()});
                            UserDetails userDetails = ent.toUserDetails();
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

    private Authentication buildAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "43dd32434d2d233",
                userDetails.getAuthorities()
        );
    }
}