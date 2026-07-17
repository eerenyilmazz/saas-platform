package com.saas.core.onboarding;

import com.saas.core.auth.entity.User;
import com.saas.core.tenant.entity.Tenant;

public interface TenantOnboardingService {

    OnboardingResult onboardTenant(String tenantName, String adminEmail, String adminPassword);

    record OnboardingResult(Tenant tenant, User admin) {
    }
}
