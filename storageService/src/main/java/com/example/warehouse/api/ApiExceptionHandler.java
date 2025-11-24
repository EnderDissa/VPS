package com.example.warehouse.api;

import com.example.warehouse.exception.*;
import jakarta.persistence.EntityNotFoundException;
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

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<Object>> handleAccessDenied(AccessDeniedException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.FORBIDDEN;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.FORBIDDEN, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(BorrowingNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleBorrowingNotFound(BorrowingNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.BORROWING_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public Mono<ResponseEntity<Object>> handleBusinessRule(BusinessRuleException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<Object>> handleConflict(ConflictException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.CONFLICT, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(DuplicateKeepingException.class)
    public Mono<ResponseEntity<Object>> handleDuplicateKeeping(DuplicateKeepingException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.DUPLICATE_KEEPING, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(DuplicateLicensePlateException.class)
    public Mono<ResponseEntity<Object>> handleDuplicateLicensePlate(DuplicateLicensePlateException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.DUPLICATE_LICENSE_PLATE, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(DuplicateSerialNumberException.class)
    public Mono<ResponseEntity<Object>> handleDuplicateSerialNumber(DuplicateSerialNumberException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.DUPLICATE_SERIAL_NUMBER, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(DuplicateStorageException.class)
    public Mono<ResponseEntity<Object>> handleDuplicateStorage(DuplicateStorageException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.DUPLICATE_STORAGE, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(DuplicateUserStorageAccessException.class)
    public Mono<ResponseEntity<Object>> handleDuplicateUserStorageAccess(DuplicateUserStorageAccessException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.DUPLICATE_USER_STORAGE_ACCESS, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(ItemMaintenanceNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleItemMaintenanceNotFound(ItemMaintenanceNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.ITEM_MAINTENANCE_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleItemNotFound(ItemNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.ITEM_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(KeepingNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleKeepingNotFound(KeepingNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.KEEPING_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public Mono<ResponseEntity<Object>> handleOperationNotAllowed(OperationNotAllowedException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.FORBIDDEN;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.OPERATION_NOT_ALLOWED, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(StorageNotEmptyException.class)
    public Mono<ResponseEntity<Object>> handleStorageNotEmpty(StorageNotEmptyException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.STORAGE_NOT_EMPTY, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(StorageNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleStorageNotFound(StorageNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.STORAGE_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(TransportationNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleTransportationNotFound(TransportationNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.TRANSPORTATION_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<Object>> handleUserAlreadyExists(UserAlreadyExistsException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.CONFLICT;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.USER_ALREADY_EXISTS, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleUserNotFound(UserNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.USER_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(UserStorageAccessNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleUserStorageAccessNotFound(UserStorageAccessNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.USER_STORAGE_ACCESS_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<Object>> handleCustomValidation(ValidationException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.BAD_REQUEST;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.VALIDATION_ERROR, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleVehicleNotFound(VehicleNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus s = HttpStatus.NOT_FOUND;
        String requestUri = exchange.getRequest().getURI().toString();
        ApiError body = ApiError.of(s.value(), s.getReasonPhrase(), ErrorCode.VEHICLE_NOT_FOUND, ex.getMessage(), requestUri);
        return Mono.just(new ResponseEntity<>(body, new HttpHeaders(), s));
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