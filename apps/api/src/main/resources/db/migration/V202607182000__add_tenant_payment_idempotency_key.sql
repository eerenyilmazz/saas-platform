-- concurrency-and-idempotency.md: a caller that retries the same payment-recording request
-- (network error, webhook redelivery) must not create a second row. Nullable + a partial unique
-- index: only flows that actually supply a key are protected, per the doc's own "sadece bu riski
-- taşıyan akışlarda zorunlu" note.
ALTER TABLE tenant_payment ADD COLUMN idempotency_key VARCHAR(255);

CREATE UNIQUE INDEX idx_tenant_payment_idempotency_key
    ON tenant_payment (tenant_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
