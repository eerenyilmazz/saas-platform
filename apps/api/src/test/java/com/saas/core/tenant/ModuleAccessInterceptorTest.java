package com.saas.core.tenant;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import com.saas.core.tenant.entity.Tenant;
import com.saas.core.tenant.repository.TenantRepository;
import com.saas.testsupport.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleAccessInterceptorTest extends AbstractIntegrationTest {

    @Autowired
    private ModuleAccessInterceptor moduleAccessInterceptor;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void aDisabledModuleIsBlockedWith404() throws Exception {
        TenantContext.setTenantId(seedTenant(false));
        var response = new MockHttpServletResponse();

        boolean proceed = moduleAccessInterceptor.preHandle(
                new MockHttpServletRequest(), response, guardedHandler());

        assertThat(proceed).isFalse();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void anEnabledModuleIsAllowedThrough() throws Exception {
        TenantContext.setTenantId(seedTenant(true));

        boolean proceed = moduleAccessInterceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), guardedHandler());

        assertThat(proceed).isTrue();
    }

    @Test
    void noTenantContextIsBlocked() throws Exception {
        boolean proceed = moduleAccessInterceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), guardedHandler());

        assertThat(proceed).isFalse();
    }

    @Test
    void aHandlerWithoutRequiresModuleAlwaysPasses() throws Exception {
        boolean proceed = moduleAccessInterceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), unguardedHandler());

        assertThat(proceed).isTrue();
    }

    @RequiresModule("fitness")
    private static final class GuardedController {
        public void handle() {
        }
    }

    private static final class UnguardedController {
        public void handle() {
        }
    }

    private HandlerMethod guardedHandler() throws NoSuchMethodException {
        return new HandlerMethod(new GuardedController(), "handle");
    }

    private HandlerMethod unguardedHandler() throws NoSuchMethodException {
        return new HandlerMethod(new UnguardedController(), "handle");
    }

    private UUID seedTenant(boolean fitnessEnabled) {
        TenantContext.setPlatformContext(true);
        try {
            Tenant tenant = new Tenant("Module Access Test Tenant");
            if (fitnessEnabled) {
                tenant.enableModule("fitness");
            }
            return tenantRepository.saveAndFlush(tenant).getId();
        } finally {
            TenantContext.setPlatformContext(false);
        }
    }
}
