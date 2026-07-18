package com.saas.modules.fitness.qr;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.saas.core.exception.BusinessRuleException;
import com.saas.core.membership.entity.Member;
import com.saas.core.membership.repository.MemberRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.core.tenant.entity.Tenant;
import com.saas.core.tenant.repository.TenantRepository;
import com.saas.modules.fitness.qr.service.QrTokenService;
import com.saas.testsupport.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Overrides app.fitness.qr.token-ttl to 0 so a test can outlast it in just over a second, instead
 * of waiting out the real 30-second default - proves expiry is actually enforced, not just
 * present in the config. The embedded timestamp (and this check) work at whole-second
 * granularity, so a sub-second TTL (e.g. 1ms) wouldn't actually be distinguishable from "not
 * expired yet" within the same second - that's adequate for the real 30-second default, but means
 * the test needs to cross a real second boundary, not just a few milliseconds.
 */
@TestPropertySource(properties = "app.fitness.qr.token-ttl=0s")
class QrTokenExpiryTest extends AbstractIntegrationTest {

    @Autowired
    private QrTokenService qrTokenService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void anExpiredTokenIsRejected() throws Exception {
        TenantContext.setPlatformContext(true);
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("QR Expiry Test Tenant"));
        TenantContext.setPlatformContext(false);

        TenantContext.setTenantId(tenant.getId());
        Member member = memberRepository.save(new Member(tenant.getId(), "Test Member", "5551234567"));

        String token = qrTokenService.issue(member.getId());
        Thread.sleep(1100);

        assertThatThrownBy(() -> qrTokenService.verify(token))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("code", "FITNESS_QR_INVALID");
    }
}
