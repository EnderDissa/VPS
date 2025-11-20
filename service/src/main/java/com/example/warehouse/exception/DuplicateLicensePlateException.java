package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLicensePlateException extends RuntimeException {
    public DuplicateLicensePlateException(String message) {
        super(message);
    }
}