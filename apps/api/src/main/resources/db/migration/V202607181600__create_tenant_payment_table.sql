-- source_id has no FK: source_type is deliberately open-ended (membership/appointment/order/...,
-- see modules/core.md) so a new module can record a payment without a core migration, and a
-- single FK column can't target more than one table. INV-4 (domain-invariants.md) still holds
-- because the project never hard-deletes rows (soft-delete only, database-conventions.md) - a
-- source_id can't go dangling through deletion, even without a DB-enforced FK on this column.
CREATE TABLE tenant_payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    source_type VARCHAR(30) NOT NULL,
    source_id UUID NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    method VARCHAR(20) NOT NULL,
    paid_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_payment_tenant_id ON tenant_payment (tenant_id);
CREATE INDEX idx_tenant_payment_source ON tenant_payment (tenant_id, source_type, source_id);

ALTER TABLE tenant_payment ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_payment FORCE ROW LEVEL SECURITY;

-- audit-log.md scope: "tenant_payment - tenant-billing kayıtları".
CREATE POLICY tenant_payment_tenant_scoped ON tenant_payment
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.support_mode', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON tenant_payment TO saas_app;
