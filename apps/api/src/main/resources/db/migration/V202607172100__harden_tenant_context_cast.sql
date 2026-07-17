-- TenantAwareDataSource (com.saas.core.tenant) stamps every pooled connection with
-- SET/RESET app.current_tenant_id on checkout, since Hikari reuses physical connections across
-- requests. RESET on an ad-hoc custom GUC (one never declared in postgresql.conf) turned out to
-- leave it as an empty string rather than truly unset - current_setting(..., true) then returns
-- '' instead of NULL, and ''::uuid raises an error instead of just making the policy deny rows.
-- NULLIF(..., '') normalizes '' to NULL first, so a reset connection behaves the same as a
-- genuinely fresh one that never had the variable set at all: comparisons involving it are NULL
-- (deny), not a thrown exception.
ALTER POLICY tenant_self_or_platform ON tenant
    USING (
        id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.platform_context', true) = 'true'
    );

ALTER POLICY user_self_or_platform ON "user"
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.platform_context', true) = 'true'
    );
