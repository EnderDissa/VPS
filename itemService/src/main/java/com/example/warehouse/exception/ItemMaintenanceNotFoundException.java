package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItemMaintenanceNotFoundException extends RuntimeException {
    public ItemMaintenanceNotFoundException(String message) {
        super(message);
    }

    public ItemMaintenanceNotFoundException(Long id) {
        super("Item maintenance not found with ID: " + id);
    }
}