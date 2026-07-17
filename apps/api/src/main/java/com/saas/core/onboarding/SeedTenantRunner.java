package com.saas.core.onboarding;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * onboarding.md's MVP flow: manual onboarding, no self-service signup, no platform admin panel
 * yet (roadmap/phases.md Faz 1). Run with:
 *
 * <pre>
 * ./gradlew bootRun --args="--spring.profiles.active=local,seed --tenant.name='Acme Gym' --admin.email=owner@acme.com"
 * </pre>
 *
 * Only active under the "seed" profile, so it never runs during normal startup or in tests. Just
 * CLI argument parsing and output - the actual onboarding is TenantOnboardingService's job.
 * --admin.password is optional - a random one is generated and printed once if omitted, which is
 * safer than typing a real password on the command line (visible in shell history/process list)
 * for credentials that get relayed to the tenant_admin by email anyway per onboarding.md step 3.
 */
@Component
@Profile("seed")
@RequiredArgsConstructor
public class SeedTenantRunner implements ApplicationRunner {

    private static final String PASSWORD_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int GENERATED_PASSWORD_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TenantOnboardingService tenantOnboardingService;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        String tenantName = requireOption(args, "tenant.name");
        String adminEmail = requireOption(args, "admin.email");
        String adminPassword = args.containsOption("admin.password")
                ? args.getOptionValues("admin.password").get(0)
                : generatePassword();

        var result = tenantOnboardingService.onboardTenant(tenantName, adminEmail, adminPassword);

        // System.out, not a logger, deliberately: a logger's output commonly ends up shipped to
        // centralized, long-retained log storage - printing a freshly generated password there
        // is worse than the terminal-only, non-persisted output this operator-run tool produces.
        System.out.println("=================================================");
        System.out.println("Tenant created:  " + result.tenant().getId() + " (" + tenantName + ")");
        System.out.println("Admin email:     " + result.admin().getEmail());
        System.out.println("Admin password:  " + adminPassword);
        System.out.println("=================================================");

        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private static String requireOption(ApplicationArguments args, String name) {
        List<String> values = args.getOptionValues(name);
        if (values == null || values.isEmpty() || values.get(0).isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required --" + name + "=... argument (see SeedTenantRunner's javadoc for usage)");
        }
        return values.get(0);
    }

    private static String generatePassword() {
        StringBuilder password = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARACTERS.charAt(RANDOM.nextInt(PASSWORD_CHARACTERS.length())));
        }
        return password.toString();
    }
}
