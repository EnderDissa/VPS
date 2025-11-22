package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserStorageAccessNotFoundException extends RuntimeException {
    public UserStorageAccessNotFoundException(String message) {
        super(message);
    }

    public UserStorageAccessNotFoundException(Long id) {
        super("User storage access not found with ID: " + id);
    }
}
