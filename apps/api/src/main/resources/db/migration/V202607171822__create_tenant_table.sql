CREATE TABLE tenant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    settings JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Row-Level Security (ADR 0008): a tenant_admin/tenant_staff session only ever sees its own
-- tenant row; a platform_owner session (operating via com.saas.platform.*) sees all rows.
-- current_setting(..., true) returns NULL instead of erroring when unset (e.g. during migration).
ALTER TABLE tenant ENABLE ROW LEVEL SECURITY;

-- FORCE also applies RLS to the table owner (the migration role). Defense in depth: the
-- application runs as saas_app (V202607171821), which is never the owner and never a
-- superuser, so RLS already applies to it without FORCE - but FORCE means this table stays
-- protected even if ownership or the connecting role ever changes.
ALTER TABLE tenant FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_self_or_platform ON tenant
    USING (
        id = current_setting('app.current_tenant_id', true)::uuid
        OR current_setting('app.platform_context', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON tenant TO saas_app;
