package com.example.warehouse.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StorageNotEmptyException extends RuntimeException {
    public StorageNotEmptyException(String message) {
        super(message);
    }
}
