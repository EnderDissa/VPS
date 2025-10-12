package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateUserStorageAccessException extends RuntimeException {
    public DuplicateUserStorageAccessException(String message) {
        super(message);
    }
}
