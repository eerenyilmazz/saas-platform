package com.saas.core.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * tenant-model.md: class-level marker on a module's controllers - ModuleAccessInterceptor checks
 * the current tenant's settings.modules.&lt;value&gt;.enabled before the request ever reaches the
 * controller, returning 404 (not 403 - a disabled module should look like it doesn't exist) if
 * it's off.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresModule {

    String value();
}
