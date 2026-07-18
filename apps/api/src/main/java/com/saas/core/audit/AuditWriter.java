package com.saas.core.audit;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.Table;

/**
 * Does the actual INSERT into audit_log. Split out from AuditListener because JPA instantiates
 * entity listener classes itself (via a no-arg constructor, outside Spring's context), so they
 * can't have dependencies @Autowired into them directly - this class is a normal Spring bean, and
 * AuditListener reaches it through the static INSTANCE field set in the constructor. The write
 * uses plain JdbcTemplate rather than the entityManager/session (which is mid-flush when
 * AuditListener's callbacks fire) specifically to avoid Hibernate session reentrancy; it still
 * lands in the same DB transaction as the change being audited because JdbcTemplate and the JPA
 * EntityManager share the same Spring-managed connection for a given DataSource.
 */
@Component
public class AuditWriter {

    private static final AtomicReference<AuditWriter> INSTANCE = new AtomicReference<>();

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public AuditWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        INSTANCE.set(this);
    }

    static void write(Object entity, String action, Map<String, Object> oldValue, Map<String, Object> newValue) {
        AuditWriter writer = INSTANCE.get();
        if (writer == null) {
            throw new IllegalStateException("AuditWriter used before Spring context initialization");
        }
        writer.doWrite(entity, action, oldValue, newValue);
    }

    private void doWrite(Object entity, String action, Map<String, Object> oldValue, Map<String, Object> newValue) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO audit_log "
                            + "(tenant_id, table_name, record_id, action, old_value, new_value, actor_user_id, actor_role) "
                            + "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?)",
                    resolveTenantId(entity),
                    tableNameOf(entity.getClass()),
                    requireFieldValue(entity, "id"),
                    action,
                    oldValue != null ? objectMapper.writeValueAsString(oldValue) : null,
                    newValue != null ? objectMapper.writeValueAsString(newValue) : null,
                    currentActorUserId(),
                    currentActorRole());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to write audit log entry for " + entity.getClass(), exception);
        }
    }

    private static Object resolveTenantId(Object entity) throws IllegalAccessException {
        return entity instanceof SelfTenantIdentity self ? self.auditTenantId() : requireFieldValue(entity, "tenantId");
    }

    private static Object requireFieldValue(Object entity, String fieldName) throws IllegalAccessException {
        Field field = findField(entity.getClass(), fieldName);
        // Same reasoning as AuditListener.snapshot(): generic access across any @Audited entity,
        // no per-entity getter to call instead.
        field.setAccessible(true);
        Object value = field.get(entity);
        if (value == null) {
            throw new IllegalStateException(fieldName + " must be set before " + entity.getClass() + " is audited");
        }
        return value;
    }

    private static Field findField(Class<?> type, String fieldName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new IllegalStateException("No " + fieldName + " field found on " + type + " or its superclasses");
    }

    private static String tableNameOf(Class<?> entityType) {
        Table table = entityType.getAnnotation(Table.class);
        if (table == null || table.name().isBlank()) {
            return entityType.getSimpleName().toLowerCase();
        }
        return table.name().replace("`", "");
    }

    private static UUID currentActorUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof UUID userId ? userId : null;
    }

    private static String currentActorRole() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                ? authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse(null)
                : null;
    }
}
