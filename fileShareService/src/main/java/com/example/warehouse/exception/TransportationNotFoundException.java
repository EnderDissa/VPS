package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TransportationNotFoundException extends RuntimeException {
    public TransportationNotFoundException(String message) {
        super(message);
    }

    public TransportationNotFoundException(Long id) {
        super("Transportation not found with ID: " + id);
    }
}
