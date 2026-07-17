-- "user" is quoted throughout because it collides with PostgreSQL's USER reserved keyword -
-- glossary.md fixes the table name as "user" (not app_user), so quoting is the only way to keep
-- that exact name and still have valid SQL.
--
-- Per glossary.md, this table holds only tenant_admin/tenant_staff (platform_owner login is a
-- separate, not-yet-built concern under com.saas.platform.*), so tenant_id is NOT NULL here -
-- unlike V202607171822's tenant table, every row belongs to exactly one tenant.
CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_user_tenant_email ON "user" (tenant_id, email);

ALTER TABLE "user" ENABLE ROW LEVEL SECURITY;
ALTER TABLE "user" FORCE ROW LEVEL SECURITY;

-- The app.platform_context branch also covers login itself: at that point in a request the
-- caller's tenant_id isn't known yet (resolving it is what login does), so
-- AuthenticationService looks a user up by email with that flag set for the single lookup query -
-- the same bypass the tenant table's own policy uses for system-level reads (ADR 0008).
CREATE POLICY user_self_or_platform ON "user"
    USING (
        tenant_id = current_setting('app.current_tenant_id', true)::uuid
        OR current_setting('app.platform_context', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON "user" TO saas_app;
