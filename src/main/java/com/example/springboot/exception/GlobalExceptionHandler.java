package com.example.springboot.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle Resource Not Found Issues (IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        // If the error relates to token expiration, map it to a 403 Forbidden for the frontend interceptor
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("token")) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden Token Session",
                    ex.getMessage()
            );
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        // Standard fallback for other missing data resources (404 Not Found)
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage()
            );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 2. Handle SQL / Database Constraint Violations (Duplicate emails, missing required fields)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request Database Constraint",
                "Invalid data payload. A required field might be missing or a duplicate entry exists."
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Global Fallback Interceptor for any generic untracked Server Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected application error occurred: " + ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}