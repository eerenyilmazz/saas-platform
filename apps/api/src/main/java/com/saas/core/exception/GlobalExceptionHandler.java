package com.saas.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.saas.core.i18n.Messages;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Messages messages;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException exception) {
        return ResponseEntity.status(exception.httpStatus())
                .body(ErrorResponse.of(exception.getCode(), exception.getMessage(), exception.getField()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        var fieldError = exception.getBindingResult().getFieldError();
        String field = fieldError != null ? fieldError.getField() : null;
        String message = fieldError != null ? fieldError.getDefaultMessage() : messages.get("VALIDATION_ERROR");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message, field));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", messages.get("INTERNAL_ERROR"), null));
    }
}
