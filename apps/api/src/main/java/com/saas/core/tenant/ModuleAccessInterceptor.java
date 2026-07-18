package com.saas.core.tenant;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.saas.core.tenant.repository.TenantRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * The single mechanism tenant-model.md describes for @RequiresModule - no per-module interceptor
 * code, this one reads the annotation off whichever controller class handled the request.
 */
@Component
@RequiredArgsConstructor
public class ModuleAccessInterceptor implements HandlerInterceptor {

    private final TenantRepository tenantRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequiresModule requiresModule = handlerMethod.getBeanType().getAnnotation(RequiresModule.class);
        if (requiresModule == null) {
            return true;
        }

        var tenantId = TenantContext.getTenantId();
        boolean enabled = tenantId != null
                && tenantRepository.findById(tenantId)
                        .map(tenant -> tenant.isModuleEnabled(requiresModule.value()))
                        .orElse(false);
        if (!enabled) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }
        return true;
    }
}
