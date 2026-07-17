package com.saas.core.onboarding;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.core.auth.entity.Role;
import com.saas.core.auth.entity.User;
import com.saas.core.auth.repository.UserRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.core.tenant.entity.Tenant;
import com.saas.core.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

/**
 * onboarding.md's manual MVP flow: a tenant plus its first tenant_admin. Currently only called
 * from SeedTenantRunner (the CLI seed script), but kept as its own service - not inlined there -
 * because onboarding.md's own roadmap has a second caller planned (self-service signup once
 * tenant count crosses ~10, v1.2), and business logic like this belongs in a service regardless
 * of how many callers it has today.
 */
@Service
@RequiredArgsConstructor
public class TenantOnboardingServiceImpl implements TenantOnboardingService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Deliberately not @Transactional: same reasoning as AuthenticationServiceImpl's
     * findUserAcrossTenants - Spring's transaction interceptor would check a connection out (and
     * TenantAwareDataSource would stamp it) at method entry, before setPlatformContext(true)
     * below ever ran. Leaving this non-transactional lets each repository call's own transaction
     * begin, and check out its connection, after the ThreadLocal is already set. The tradeoff:
     * the two saves aren't atomic - a failure between them would leave a tenant with no admin.
     * Acceptable here (an internal, operator-run tool with a small, mostly-unreachable failure
     * window), not a pattern to copy for a customer-facing multi-step write.
     */
    @Override
    public OnboardingResult onboardTenant(String tenantName, String adminEmail, String adminPassword) {
        TenantContext.setPlatformContext(true);
        try {
            Tenant tenant = tenantRepository.save(new Tenant(tenantName));
            User admin = userRepository.save(
                    new User(tenant.getId(), adminEmail, passwordEncoder.encode(adminPassword), Role.TENANT_ADMIN));
            return new OnboardingResult(tenant, admin);
        } finally {
            TenantContext.setPlatformContext(false);
        }
    }
}
