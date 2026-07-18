-- Refresh tokens carry no tenant context on their own - a client presents only the raw token,
-- so both the token-hash lookup and the user lookup it resolves to need the same
-- app.platform_context bypass "user" (V202607172044) uses for login: resolving the tenant is
-- the whole point of the call. Written with the NULLIF-hardened comparison from the start (see
-- V202607172100's lesson) instead of retrofitting it later.
CREATE TABLE refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    user_id UUID NOT NULL REFERENCES "user"(id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_refresh_token_hash ON refresh_token (token_hash);
CREATE INDEX idx_refresh_token_tenant_id ON refresh_token (tenant_id);
CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);

ALTER TABLE refresh_token ENABLE ROW LEVEL SECURITY;
ALTER TABLE refresh_token FORCE ROW LEVEL SECURITY;

CREATE POLICY refresh_token_self_or_platform ON refresh_token
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        OR current_setting('app.platform_context', true) = 'true'
    );

GRANT SELECT, INSERT, UPDATE, DELETE ON refresh_token TO saas_app;
