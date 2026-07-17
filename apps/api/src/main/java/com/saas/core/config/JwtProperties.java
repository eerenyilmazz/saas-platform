package com.saas.core.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Bound from app.jwt.* (application-local.properties for local dev, JWT_SECRET/env vars in real
 * deployments - see security.md). No value here is ever a literal in Java code, per code-review
 * secrets rule.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private Duration accessTokenTtl = Duration.ofMinutes(20);
}
