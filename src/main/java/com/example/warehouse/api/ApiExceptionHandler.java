package com.example.warehouse.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.INVALID_JSON, "Malformed JSON request", req.getRequestURI());
        if (ex.getMostSpecificCause() != null) body.addDetail(ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(s).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.MISSING_REQUEST_PARAM, "Required request parameter is missing", req.getRequestURI());
        body.addDetail("Parameter '" + ex.getParameterName() + "' of type '" + ex.getParameterType() + "' is required");
        return ResponseEntity.status(s).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.TYPE_MISMATCH, "Request parameter has wrong type", req.getRequestURI());
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        body.addDetail("Parameter '" + ex.getName() + "' must be of type '" + required + "'");
        return ResponseEntity.status(s).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.VALIDATION_ERROR, "Validation failed", req.getRequestURI());
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            body.addValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(s).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Entity not found", req.getRequestURI());
        return ResponseEntity.status(s).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.CONFLICT;
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.DATA_INTEGRITY_VIOLATION, "Data integrity violation", req.getRequestURI());
        if (ex.getMostSpecificCause() != null) body.addDetail(ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(s).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.INTERNAL_ERROR, "Unexpected error", req.getRequestURI());
        return ResponseEntity.status(s).body(body);
    }
}
