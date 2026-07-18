package com.saas.modules.fitness.checkin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.core.auth.entity.Role;
import com.saas.core.auth.entity.User;
import com.saas.core.auth.repository.UserRepository;
import com.saas.core.membership.entity.Member;
import com.saas.core.membership.entity.Membership;
import com.saas.core.membership.entity.MembershipPlan;
import com.saas.core.membership.repository.MemberRepository;
import com.saas.core.membership.repository.MembershipPlanRepository;
import com.saas.core.membership.repository.MembershipRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.core.tenant.entity.Tenant;
import com.saas.core.tenant.repository.TenantRepository;
import com.saas.testsupport.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the whole Faz 2 MVP flow through real HTTP: a logged-in staff member requests a QR
 * for a member, then "scans" it via the checkin endpoint - proving @RequiresModule, the stateless
 * HMAC QR round trip, and the active-membership check all work together, not just in isolation.
 */
@AutoConfigureMockMvc
class FitnessCheckinIntegrationTest extends AbstractIntegrationTest {

    private static final String STAFF_PASSWORD = "correct-horse-battery-staple";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void aMemberWithAnActiveMembershipCanCheckIn() throws Exception {
        UUID tenantId = seedTenant(true);
        String accessToken = seedStaffAndLogIn(tenantId);
        UUID memberId = seedMember(tenantId);
        seedActiveMembership(tenantId, memberId);

        String qrToken = issueQrToken(accessToken, memberId);

        mockMvc.perform(post("/api/v1/fitness/checkins")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"token\":\"" + qrToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.checkedInAt").isNotEmpty());
    }

    @Test
    void aMemberWithNoActiveMembershipCannotCheckIn() throws Exception {
        UUID tenantId = seedTenant(true);
        String accessToken = seedStaffAndLogIn(tenantId);
        UUID memberId = seedMember(tenantId);

        String qrToken = issueQrToken(accessToken, memberId);

        mockMvc.perform(post("/api/v1/fitness/checkins")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"token\":\"" + qrToken + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("FITNESS_MEMBERSHIP_INACTIVE"));
    }

    @Test
    void anInvalidQrTokenIsRejected() throws Exception {
        UUID tenantId = seedTenant(true);
        String accessToken = seedStaffAndLogIn(tenantId);

        mockMvc.perform(post("/api/v1/fitness/checkins")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"token\":\"not-a-real-token\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("FITNESS_QR_INVALID"));
    }

    @Test
    void fitnessEndpointsAreNotFoundWhenTheModuleIsDisabled() throws Exception {
        UUID tenantId = seedTenant(false);
        String accessToken = seedStaffAndLogIn(tenantId);

        mockMvc.perform(post("/api/v1/fitness/checkins")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"token\":\"irrelevant\"}"))
                .andExpect(status().isNotFound());
    }

    private UUID seedTenant(boolean fitnessEnabled) {
        TenantContext.setPlatformContext(true);
        try {
            Tenant tenant = new Tenant("Fitness Test Tenant");
            if (fitnessEnabled) {
                tenant.enableModule("fitness");
            }
            return tenantRepository.saveAndFlush(tenant).getId();
        } finally {
            TenantContext.setPlatformContext(false);
        }
    }

    private String seedStaffAndLogIn(UUID tenantId) throws Exception {
        String email = "staff-" + UUID.randomUUID() + "@example.com";
        TenantContext.setTenantId(tenantId);
        try {
            userRepository.save(new User(tenantId, email, passwordEncoder.encode(STAFF_PASSWORD), Role.TENANT_STAFF));
        } finally {
            TenantContext.clear();
        }

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + STAFF_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private UUID seedMember(UUID tenantId) {
        TenantContext.setTenantId(tenantId);
        try {
            return memberRepository.save(new Member(tenantId, "Test Member", "5551234567")).getId();
        } finally {
            TenantContext.clear();
        }
    }

    private void seedActiveMembership(UUID tenantId, UUID memberId) {
        TenantContext.setTenantId(tenantId);
        try {
            MembershipPlan plan = membershipPlanRepository.save(new MembershipPlan(tenantId, "Monthly", 30, null));
            Instant now = Instant.now();
            membershipRepository.save(new Membership(tenantId, memberId, plan.getId(), now, now.plus(30, ChronoUnit.DAYS)));
        } finally {
            TenantContext.clear();
        }
    }

    private String issueQrToken(String accessToken, UUID memberId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/fitness/members/" + memberId + "/qr-token")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
