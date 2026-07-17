package com.saas.core.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bound from app.login-rate-limit.* - security.md's brute-force protection: lock out after
 * maxAttempts failed logins (per IP and per account, independently) within window, for
 * lockoutDuration. Defaults match the doc's own suggested numbers (5 / 15 minutes); window
 * defaults to 1 minute since the doc says "aynı IP'den/aynı hesaba dakikada... 5 deneme".
 */
@Component
@ConfigurationProperties(prefix = "app.login-rate-limit")
public class LoginRateLimitProperties {

    private int maxAttempts = 5;
    private Duration window = Duration.ofMinutes(1);
    private Duration lockoutDuration = Duration.ofMinutes(15);

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Duration getWindow() {
        return window;
    }

    public void setWindow(Duration window) {
        this.window = window;
    }

    public Duration getLockoutDuration() {
        return lockoutDuration;
    }

    public void setLockoutDuration(Duration lockoutDuration) {
        this.lockoutDuration = lockoutDuration;
    }
}
