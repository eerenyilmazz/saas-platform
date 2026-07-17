package com.saas.core.exception;

public record ErrorResponse(ErrorDetail error) {

    public static ErrorResponse of(String code, String message, String field) {
        return new ErrorResponse(new ErrorDetail(code, message, field));
    }
}
