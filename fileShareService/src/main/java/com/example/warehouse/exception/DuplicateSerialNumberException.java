package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateSerialNumberException extends RuntimeException {
    public DuplicateSerialNumberException(String message) {
        super(message);
    }
}
