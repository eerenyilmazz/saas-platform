package com.saas.core.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.core.auth.dto.LoginRequest;
import com.saas.core.auth.dto.LoginResponse;
import com.saas.core.auth.security.LoginAttemptTracker;
import com.saas.core.auth.service.AuthenticationService;
import com.saas.core.exception.ForbiddenException;
import com.saas.core.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final LoginAttemptTracker loginAttemptTracker;

    public AuthController(AuthenticationService authenticationService, LoginAttemptTracker loginAttemptTracker) {
        this.authenticationService = authenticationService;
        this.loginAttemptTracker = loginAttemptTracker;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipKey = "ip:" + httpRequest.getRemoteAddr();
        String emailKey = "email:" + request.email();

        if (loginAttemptTracker.isLocked(ipKey) || loginAttemptTracker.isLocked(emailKey)) {
            // 403, not the semantically closer 429: error-handling.md's exception hierarchy is a
            // fixed set of 6 types with no rate-limit entry, and forbids adding a 7th.
            throw new ForbiddenException("AUTH_TOO_MANY_ATTEMPTS",
                    "Çok fazla başarısız deneme. Lütfen daha sonra tekrar deneyin.");
        }

        try {
            LoginResponse response = authenticationService.login(request);
            loginAttemptTracker.recordSuccess(ipKey);
            loginAttemptTracker.recordSuccess(emailKey);
            return response;
        } catch (UnauthorizedException exception) {
            loginAttemptTracker.recordFailure(ipKey);
            loginAttemptTracker.recordFailure(emailKey);
            throw exception;
        }
    }
}
