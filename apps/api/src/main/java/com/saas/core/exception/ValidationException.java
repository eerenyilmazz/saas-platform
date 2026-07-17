package com.saas.core.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends DomainException {

    public ValidationException(String code, String message, String field) {
        super(code, message, field);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
