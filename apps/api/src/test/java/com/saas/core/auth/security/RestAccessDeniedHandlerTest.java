package com.saas.core.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.saas.testsupport.TestMessages;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * There is no role-restricted endpoint yet (ADR 0011's context: only /api/v1/auth/** exists
 * today, and it's all permitAll), so AccessDeniedException can't currently be triggered through a
 * real HTTP request - this exercises the handler directly instead of waiting for Faz 2's first
 * role-restricted controller.
 */
class RestAccessDeniedHandlerTest {

    private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler(TestMessages.create());

    @Test
    void writesTheStandardErrorEnvelopeWith403() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString())
                .contains("\"AUTH_FORBIDDEN\"")
                .contains("Bu işlem için yetkiniz yok.");
    }
}
