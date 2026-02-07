package com.example.warehouse.infrastructure.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsEntity implements Serializable {
    private static final long serialVersionUID = 3446634L;

    private String password;
    private String username;
    private String[] authorities;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Boolean enabled;

    public UserDetailsEntity() {
    }

    public UserDetailsEntity(String username, String password, String[] auth){
        this.username = username;
        this.password = password;
        this.accountNonExpired = true;
        this.enabled = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.authorities = auth;
    }

    public UserDetails toUserDetails() {
        if (username == null || password == null) {
            return null;
        }

        List<GrantedAuthority> authorityList = authorities != null && authorities.length > 0
                ? Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList())
                : List.of(new SimpleGrantedAuthority("USER"));

        return User.builder()
                .username(username)
                .password(password)
                .authorities(authorityList)
                .accountExpired(accountNonExpired == null || !accountNonExpired)
                .accountLocked(accountNonLocked == null || !accountNonLocked)
                .credentialsExpired(credentialsNonExpired == null || !credentialsNonExpired)
                .disabled(enabled == null || !enabled)
                .build();
    }

    @Override
    public String toString() {
        return "UserDetailsEntity{" +
                "username='" + username + '\'' +
                ", authorities=" + Arrays.toString(authorities) +
                '}';
    }
}