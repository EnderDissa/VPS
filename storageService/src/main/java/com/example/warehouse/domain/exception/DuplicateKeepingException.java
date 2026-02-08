package com.example.warehouse.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateKeepingException extends RuntimeException {
    public DuplicateKeepingException(String message) {
        super(message);
    }
}
