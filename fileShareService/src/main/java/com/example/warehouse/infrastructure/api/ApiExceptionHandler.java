package com.example.warehouse.infrastructure.api;

import com.example.warehouse.exception.AccessDeniedException;
import com.example.warehouse.exception.BorrowingNotFoundException;
import com.example.warehouse.exception.BusinessRuleException;
import com.example.warehouse.exception.ConflictException;
import com.example.warehouse.exception.DuplicateKeepingException;
import com.example.warehouse.exception.DuplicateLicensePlateException;
import com.example.warehouse.exception.DuplicateSerialNumberException;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.DuplicateUserStorageAccessException;
import com.example.warehouse.exception.ItemMaintenanceNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.KeepingNotFoundException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.TransportationNotFoundException;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.exception.UserStorageAccessNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.exception.VehicleNotFoundException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import jakarta.persistence.EntityNotFoundException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(
            WebExchangeBindException ex, HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.VALIDATION_ERROR, "Validation failed", requestUri);

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            body.addValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage());
        }
        for (org.springframework.validation.ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
            body.addDetail(oe.getObjectName() + ": " + oe.getDefaultMessage());
        }

        return Mono.just(new ResponseEntity<>(body, headers, status));
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleServerWebInputException(
            ServerWebInputException ex, HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.INVALID_JSON, "Malformed JSON request", requestUri);
        body.addDetail(ex.getReason() != null ? ex.getReason() : "Input error");

        return Mono.just(new ResponseEntity<>(body, headers, status));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<ResponseEntity<ApiError>> handleInvalidJson(HttpMessageNotReadableException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.INVALID_JSON, "Malformed JSON request", requestUri);
        if (ex.getMostSpecificCause() != null) body.addDetail(ex.getMostSpecificCause().getMessage());
        return Mono.just(ResponseEntity.status(s).body(body));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Mono<ResponseEntity<ApiError>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.TYPE_MISMATCH, "Request parameter has wrong type", requestUri);
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        body.addDetail("Parameter '" + ex.getName() + "' must be of type '" + required + "'");
        return Mono.just(ResponseEntity.status(s).body(body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ApiError>> handleValidation(MethodArgumentNotValidException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.VALIDATION_ERROR, "Validation failed", requestUri);
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            body.addValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage());
        }
        return Mono.just(ResponseEntity.status(s).body(body));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNotFound(EntityNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Entity not found", requestUri);
        return Mono.just(ResponseEntity.status(s).body(body));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ApiError>> handleDataIntegrity(DataIntegrityViolationException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.DATA_INTEGRITY_VIOLATION, "Data integrity violation", requestUri);
        if (ex.getMostSpecificCause() != null) body.addDetail(ex.getMostSpecificCause().getMessage());
        return Mono.just(ResponseEntity.status(s).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleOther(Exception ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        String message = ex.getMessage();
        if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            errorCode = ErrorCode.FORBIDDEN;
        } else if (ex instanceof BorrowingNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.BORROWING_NOT_FOUND;
        } else if (ex instanceof BusinessRuleException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ErrorCode.BUSINESS_RULE_VIOLATION;
        } else if (ex instanceof ConflictException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.CONFLICT;
        } else if (ex instanceof DuplicateKeepingException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.DUPLICATE_KEEPING;
        } else if (ex instanceof DuplicateLicensePlateException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.DUPLICATE_LICENSE_PLATE;
        } else if (ex instanceof DuplicateSerialNumberException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.DUPLICATE_SERIAL_NUMBER;
        } else if (ex instanceof DuplicateStorageException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.DUPLICATE_STORAGE;
        } else if (ex instanceof DuplicateUserStorageAccessException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.DUPLICATE_USER_STORAGE_ACCESS;
        } else if (ex instanceof ItemMaintenanceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.ITEM_MAINTENANCE_NOT_FOUND;
        } else if (ex instanceof ItemNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.ITEM_NOT_FOUND;
        } else if (ex instanceof KeepingNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.KEEPING_NOT_FOUND;
        } else if (ex instanceof OperationNotAllowedException) {
            status = HttpStatus.FORBIDDEN;
            errorCode = ErrorCode.OPERATION_NOT_ALLOWED;
        } else if (ex instanceof StorageNotEmptyException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.STORAGE_NOT_EMPTY;
        } else if (ex instanceof StorageNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.STORAGE_NOT_FOUND;
        } else if (ex instanceof TransportationNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.TRANSPORTATION_NOT_FOUND;
        } else if (ex instanceof UserAlreadyExistsException) {
            status = HttpStatus.CONFLICT;
            errorCode = ErrorCode.USER_ALREADY_EXISTS;
        } else if (ex instanceof UserNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.USER_NOT_FOUND;
        } else if (ex instanceof UserStorageAccessNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.USER_STORAGE_ACCESS_NOT_FOUND;
        } else if (ex instanceof ValidationException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ErrorCode.VALIDATION_ERROR;
            // Use field-specific message if available
        } else if (ex instanceof VehicleNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.VEHICLE_NOT_FOUND;
        }

        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), errorCode, message != null ? message : "Internal server error", requestUri);
        ex.printStackTrace();
        return Mono.just(ResponseEntity.status(status).body(body));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<Object>> handleUnexpected(Throwable ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.INTERNAL_SERVER_ERROR;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }
}
