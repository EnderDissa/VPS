package com.example.warehouse.infrastructure.auth;


import com.example.warehouse.infrastructure.auth.communication.RestServiceCommunicator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CrossServiceUserRepository {

    private final RestServiceCommunicator communicator;

    @Autowired
    public CrossServiceUserRepository(RestServiceCommunicator communicator) {
        this.communicator = communicator;
    }

    public UserDetailsService getUserDetailsService() {
        return username -> {
            System.out.println("Getting user " + username);
            AuthRequest response = communicator.executeRequest(
                            new AuthRequest(RequestType.GET_USER, username))
                    .share()
                    .block();

            System.out.println("Received user " + response);

            if (response == null) {
                throw new UsernameNotFoundException("No response received for user: " + username);
            }

            if (response.getStatus() == AuthRequest.SUCCESS) {
                return (User) response.getResult();
            } else {
                throw new UsernameNotFoundException((String) response.getResult());
            }
        };
    }

    public UserDetailsPasswordService getUserDetailsPasswordService() {
        return (userDetails, newPassword) -> {
            UserDetails newDetails = User.withUsername(userDetails.getUsername())
                    .password(newPassword)
                    .roles("USER")
                    .build();

            AuthRequest response = communicator.executeRequest(
                            new AuthRequest(RequestType.CHANGE_PASSWORD, newDetails))
                    .share()
                    .block();

            if (response == null || response.getStatus() != AuthRequest.SUCCESS) {
                throw new RuntimeException("Failed to update password: " +
                        (response != null ? response.getResult() : "No response"));
            }

            return (UserDetails) response.getResult();
        };
    }
}