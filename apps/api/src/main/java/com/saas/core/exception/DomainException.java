package com.saas.core.exception;

import org.springframework.http.HttpStatus;

public abstract class DomainException extends RuntimeException {

    private final String code;
    private final String field;

    protected DomainException(String code, String message) {
        this(code, message, null);
    }

    protected DomainException(String code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public abstract HttpStatus httpStatus();

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }
}
