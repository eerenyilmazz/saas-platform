CREATE TABLE membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    member_id UUID NOT NULL REFERENCES member(id),
    plan_id UUID NOT NULL REFERENCES membership_plan(id),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_membership_tenant_id ON membership (tenant_id);
CREATE INDEX idx_membership_tenant_member ON membership (tenant_id, member_id);
CREATE INDEX idx_membership_plan_id ON membership (plan_id);

-- INV-1 (domain-invariants.md): a member can't hold more than one ACTIVE membership in the same
-- tenant at once. A partial unique index only constrains rows matching the WHERE clause, so any
-- number of FROZEN/EXPIRED rows can coexist for the same member - only a second ACTIVE one is
-- rejected.
CREATE UNIQUE INDEX idx_membership_one_active_per_member
    ON membership (tenant_id, member_id)
    WHERE status = 'ACTIVE';

ALTER TABLE membership ENABLE ROW LEVEL SECURITY;
ALTER TABLE membership FORCE ROW LEVEL SECURITY;

-- audit-log.md scope: "membership, membership_plan - üyelik kaydı/plan değişiklikleri".
CREATE POLICY membership_tenant_scoped ON membership
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.support_mode', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON membership TO saas_app;
