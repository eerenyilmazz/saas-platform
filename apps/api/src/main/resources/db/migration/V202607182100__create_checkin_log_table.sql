-- fitness.md's Faz 2 MVP: QR check-in. audit-log.md explicitly excludes this table from the
-- audit mechanism - a checkin_log row already IS the event record, a second audit trail of it
-- would just duplicate the same fact.
CREATE TABLE checkin_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    member_id UUID NOT NULL REFERENCES member(id),
    membership_id UUID NOT NULL REFERENCES membership(id),
    checked_in_at TIMESTAMPTZ NOT NULL,
    checked_out_at TIMESTAMPTZ
);

CREATE INDEX idx_checkin_log_tenant_id ON checkin_log (tenant_id);
CREATE INDEX idx_checkin_log_tenant_member ON checkin_log (tenant_id, member_id, checked_in_at);
CREATE INDEX idx_checkin_log_membership_id ON checkin_log (membership_id);

ALTER TABLE checkin_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE checkin_log FORCE ROW LEVEL SECURITY;

CREATE POLICY checkin_log_tenant_scoped ON checkin_log
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.support_mode', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON checkin_log TO saas_app;
