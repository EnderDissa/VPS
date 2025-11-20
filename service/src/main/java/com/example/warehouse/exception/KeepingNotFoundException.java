package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class KeepingNotFoundException extends RuntimeException {
    public KeepingNotFoundException(String message) {
        super(message);
    }

    public KeepingNotFoundException(Long id) {
        super("Keeping record not found with ID: " + id);
    }
}