package com.saas.core.auth.service;

import com.saas.core.auth.dto.LoginRequest;
import com.saas.core.auth.dto.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request, String clientIp);

    LoginResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
