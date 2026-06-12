package com.example.it211project.advice;

import com.example.it211project.dto.response.ApiDataResponse;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class APIControllerAdvice {

    /**
     * 404 - Resource not found
     */
    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiDataResponse<String>> handleNotFoundException(
            RuntimeException ex) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        false,
                        "Resource not found",
                        null,
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND
                ),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * 409 - Conflict / Duplicate data
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiDataResponse<String>> handleDuplicateException(
            DuplicateResourceException ex) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        false,
                        "Conflict",
                        null,
                        ex.getMessage(),
                        HttpStatus.CONFLICT
                ),
                HttpStatus.CONFLICT
        );
    }

    /**
     * 400 - Validation error (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiDataResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        false,
                        "Validation failed",
                        null,
                        errors,
                        HttpStatus.BAD_REQUEST
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 400 - Lỗi logic từ service (IllegalArgumentException)
     * VD: sai mật khẩu cũ, loại file không hợp lệ, v.v.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiDataResponse<String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        false,
                        "Bad request",
                        null,
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 401 - Lỗi xác thực token
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiDataResponse<String>> handleRuntimeException(
            RuntimeException ex) {

        String message = ex.getMessage();
        // Token expired / revoked -> 401
        if (message != null && (message.contains("expired") || message.contains("revoked"))) {
            return new ResponseEntity<>(
                    new ApiDataResponse<>(
                            false,
                            "Unauthorized",
                            null,
                            message,
                            HttpStatus.UNAUTHORIZED
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        false,
                        "Internal server error",
                        null,
                        message,
                        HttpStatus.INTERNAL_SERVER_ERROR
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * 500 - Lỗi chung không xác định
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiDataResponse<String>> handleException(
            Exception ex) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        false,
                        "Internal server error",
                        null,
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}