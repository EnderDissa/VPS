package com.mastik.gateway.auth;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class RemoteAuthenticationManager implements AuthenticationManager {

    @Autowired
    CrossServiceUserRepository userRepository = null;

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (authentication.getPrincipal() == null) return null;
        UserDetails user = User.withUsername((String) authentication.getPrincipal()).password((String) authentication.getCredentials()).build();
        try {
            if (userRepository.getUserDetailsService()
                    .loadUserByUsername(user.getUsername()) == user
            ) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        user,
                        authentication.getCredentials(),
                        List.of((GrantedAuthority) () -> "USER"));
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(auth);
                return auth;
            } else {
                throw new BadCredentialsException("bad credentials");
            }
        } catch (UsernameNotFoundException e) {
            return null;

        }
    }
}