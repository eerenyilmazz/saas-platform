package com.saas.core.exception;

public record ErrorDetail(String code, String message, String field) {
}
