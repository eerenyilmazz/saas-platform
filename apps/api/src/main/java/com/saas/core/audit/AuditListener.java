package com.saas.core.audit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

/**
 * Attach with @EntityListeners(AuditListener.class) on any entity audit-log.md lists as needing a
 * trail (membership, tenant_payment, user, ...) - no other per-entity code required. Fields
 * annotated @AuditSensitive are redacted in the JSON snapshot instead of omitted, so the trail
 * still shows a change happened without leaking the value.
 *
 * UPDATE's old_value comes from a snapshot taken on @PostLoad, since plain JPA callbacks (unlike
 * Hibernate's own Interceptor.onFlushDirty) only ever see the entity's current state - there's no
 * built-in access to what it looked like before this change. The snapshot cache is a static,
 * weakly-referenced map keyed by entity instance: JPA providers construct listener instances
 * themselves (see AuditWriter's javadoc), so this class can't hold per-request state on `this`.
 *
 * Only works for entities with no lazy associations - reflecting over a field mid-flush that
 * turns out to be an uninitialized lazy proxy would trigger a query (or fail outside a session).
 * Fine for User today; a future @Audited entity with a @ManyToOne would need this revisited.
 */
public class AuditListener {

    private static final Map<Object, Map<String, Object>> SNAPSHOTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @PostLoad
    public void postLoad(Object entity) {
        SNAPSHOTS.put(entity, snapshot(entity));
    }

    @PostPersist
    public void postPersist(Object entity) {
        AuditWriter.write(entity, "create", null, snapshot(entity));
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        AuditWriter.write(entity, "update", SNAPSHOTS.get(entity), snapshot(entity));
    }

    @PreRemove
    public void preRemove(Object entity) {
        AuditWriter.write(entity, "delete", snapshot(entity), null);
    }

    private Map<String, Object> snapshot(Object entity) {
        Map<String, Object> values = new HashMap<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            // Needed: this walks arbitrary @Audited entities' private fields generically, there's
            // no per-entity getter list to fall back on.
            field.setAccessible(true);
            try {
                values.put(field.getName(),
                        field.isAnnotationPresent(AuditSensitive.class) ? "***REDACTED***" : field.get(entity));
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }
        }
        return values;
    }
}
