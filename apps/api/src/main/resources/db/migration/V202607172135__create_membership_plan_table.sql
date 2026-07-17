-- entry_allowance NULL = unlimited entries for the duration (fitness's typical "unlimited"
-- package); a set number otherwise (e.g. a 10-visit package).
CREATE TABLE membership_plan (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    name VARCHAR(200) NOT NULL,
    duration_days INTEGER NOT NULL,
    entry_allowance INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_membership_plan_tenant_id ON membership_plan (tenant_id);

ALTER TABLE membership_plan ENABLE ROW LEVEL SECURITY;
ALTER TABLE membership_plan FORCE ROW LEVEL SECURITY;

-- audit-log.md scope: "membership, membership_plan - üyelik kaydı/plan değişiklikleri".
CREATE POLICY membership_plan_tenant_scoped ON membership_plan
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.support_mode', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON membership_plan TO saas_app;
