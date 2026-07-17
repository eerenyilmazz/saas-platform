-- member != user (glossary.md): the tenant's own customer, not someone who logs into the system.
-- user_id is nullable - only set when the member has their own login (e.g. fitness's QR app),
-- most berber-style customers never get one.
CREATE TABLE member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(30),
    consent_at TIMESTAMPTZ,
    user_id UUID REFERENCES "user"(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_member_tenant_id ON member (tenant_id);
CREATE INDEX idx_member_user_id ON member (user_id);

ALTER TABLE member ENABLE ROW LEVEL SECURITY;
ALTER TABLE member FORCE ROW LEVEL SECURITY;

-- Not audited (audit-log.md's scope list is membership/membership_plan/tenant_payment/user/
-- tenant.settings - member itself isn't on it).
--
-- app.support_mode, not app.platform_context: per tenant-isolation-enforcement.md's table, the
-- platform_context flag is only for the tenant table itself (routine tenant management);
-- member is exactly the "diğer tüm tenant-scoped tablolar" case support-access.md's own example
-- covers ("üyemi bulamıyorum" support call). That flow (support_session, required reason) isn't
-- built yet - this just declares the RLS contract it'll rely on, the same way the tenant table's
-- policy referenced app.platform_context before any platform_owner endpoint existed.
CREATE POLICY member_tenant_scoped ON member
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.support_mode', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON member TO saas_app;
