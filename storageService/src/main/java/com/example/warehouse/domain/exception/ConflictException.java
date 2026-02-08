package com.example.warehouse.domain.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
