package com.saas.core.auth.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.saas.core.config.LoginRateLimitProperties;

/**
 * security.md's brute-force protection: after maxAttempts failed logins for the same key within
 * window, lock that key out for lockoutDuration. AuthController calls this with two independent
 * keys per request (client IP, attempted email) so either one tripping blocks the request -
 * otherwise an attacker rotating emails from one IP, or rotating IPs against one account, would
 * each look fine to a single-dimension tracker.
 *
 * In-memory, not persisted or shared across instances - fine for a single app instance (today's
 * deployment), not for a horizontally-scaled one, and entries for keys that stop being used are
 * never evicted (a bounded MVP tradeoff, not a resolved one - a real cleanup pass or moving this
 * to Redis is future work once either concern actually matters).
 */
@Component
public class LoginAttemptTracker {

    private final LoginRateLimitProperties properties;
    private final Map<String, AttemptState> states = new ConcurrentHashMap<>();

    public LoginAttemptTracker(LoginRateLimitProperties properties) {
        this.properties = properties;
    }

    public boolean isLocked(String key) {
        AttemptState state = states.get(key);
        return state != null && state.lockedUntil() != null && Instant.now().isBefore(state.lockedUntil());
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        states.compute(key, (ignoredKey, existing) -> {
            if (existing != null && existing.lockedUntil() != null && now.isBefore(existing.lockedUntil())) {
                return existing;
            }
            boolean windowStillOpen = existing != null && now.isBefore(existing.windowStart().plus(properties.getWindow()));
            AttemptState base = windowStillOpen ? existing : new AttemptState(now, 0, null);
            int count = base.count() + 1;
            Instant lockedUntil = count >= properties.getMaxAttempts() ? now.plus(properties.getLockoutDuration()) : null;
            return new AttemptState(base.windowStart(), count, lockedUntil);
        });
    }

    public void recordSuccess(String key) {
        states.remove(key);
    }

    /** Test-only: this bean is a singleton shared by every test method in the same Spring
     * context, so state from one test (e.g. an IP-based lock) would otherwise leak into the
     * next. */
    void clear() {
        states.clear();
    }

    private record AttemptState(Instant windowStart, int count, Instant lockedUntil) {
    }
}
