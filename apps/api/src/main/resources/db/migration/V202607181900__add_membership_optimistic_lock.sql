-- concurrency-and-idempotency.md: two staff updating the same membership at once (one freezing
-- it while the other renews) must not let the later write silently overwrite the earlier one.
ALTER TABLE membership ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
