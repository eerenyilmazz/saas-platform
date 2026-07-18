package com.saas.core.auth.dto;

public record LoginResponse(String accessToken, String refreshToken) {
}
