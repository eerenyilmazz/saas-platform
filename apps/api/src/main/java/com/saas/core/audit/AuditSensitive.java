package com.saas.core.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field AuditListener must redact instead of writing its real value into audit_log's
 * old_value/new_value JSON - e.g. User.passwordHash. Redaction, not omission: the field still
 * shows up in the trail (a password WAS changed), just not what it changed to/from.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AuditSensitive {
}
