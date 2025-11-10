package com.example.warehouse.api;

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

import java.util.Arrays;

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


        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), errorCode, message != null ? message : "Internal server error", req.getRequestURI());
        ex.printStackTrace();
        return ResponseEntity.status(status).body(body);
    }
}
