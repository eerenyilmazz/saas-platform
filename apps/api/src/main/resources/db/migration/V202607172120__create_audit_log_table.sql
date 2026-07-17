-- Central audit trail (audit-log.md) - built before membership/tenant_payment exist so those
-- tables are audited from their very first migration instead of retrofitted later.
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    table_name VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    action VARCHAR(10) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    actor_user_id UUID,
    actor_role VARCHAR(30),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_tenant_id ON audit_log (tenant_id);
CREATE INDEX idx_audit_log_table_record ON audit_log (table_name, record_id);

ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log FORCE ROW LEVEL SECURITY;

CREATE POLICY audit_log_self_or_platform ON audit_log
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.platform_context', true) = 'true'
    );

-- SELECT + INSERT only, deliberately no UPDATE/DELETE: an audit trail that the application role
-- can modify after the fact isn't one - corrections happen via a new row, never by rewriting
-- history.
GRANT SELECT, INSERT ON audit_log TO saas_app;
