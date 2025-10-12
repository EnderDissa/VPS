package com.example.warehouse.api;

import com.example.warehouse.exception.BusinessRuleException;
import com.example.warehouse.exception.ConflictException;
import com.example.warehouse.exception.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiErrorField(f.getField(), f.getDefaultMessage()))
                .toList();
        return new ApiErrorResponse("VALIDATION_ERROR", "Validation failed", fields);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        var fields = ex.getConstraintViolations().stream()
                .map(v -> new ApiErrorField(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return new ApiErrorResponse("VALIDATION_ERROR", "Validation failed", fields);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ApiErrorResponse handleBadRequest(Exception ex) {
        return new ApiErrorResponse("BAD_REQUEST", ex.getMessage(), List.of());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ NotFoundException.class, EntityNotFoundException.class })
    public ApiErrorResponse handleNotFound(Exception ex) {
        return new ApiErrorResponse("NOT_FOUND", ex.getMessage(), List.of());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ApiErrorResponse handleConflict(ConflictException ex) {
        return new ApiErrorResponse("CONFLICT", ex.getMessage(), List.of());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(BusinessRuleException.class)
    public ApiErrorResponse handleBusiness(BusinessRuleException ex) {
        return new ApiErrorResponse("BUSINESS_RULE", ex.getMessage(), List.of());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiErrorResponse handleGeneric(Exception ex) {
        return new ApiErrorResponse("INTERNAL_ERROR", ex.getMessage(), List.of());
    }
}
