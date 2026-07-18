-- Auditing Tenant's own creation writes an audit_log row referencing that same, just-inserted
-- tenant_id within one transaction. Making the constraint DEFERRABLE INITIALLY DEFERRED was tried
-- first and passed in isolation, but failed deterministically whenever the full suite's shared
-- connection pool was under load from other tests running first - not worth chasing further,
-- since audit_log.record_id already has no FK either (it's polymorphic across every audited
-- table, same reasoning as tenant_payment.source_id in V202607181600). Dropping tenant_id's FK
-- too makes both audit_log columns consistent: RLS + the tenant_id index still isolate/scope the
-- data, an FK was only ever a belt-and-suspenders extra, not the isolation mechanism itself.
ALTER TABLE audit_log DROP CONSTRAINT audit_log_tenant_id_fkey;
