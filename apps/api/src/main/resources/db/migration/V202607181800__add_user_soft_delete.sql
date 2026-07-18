-- Establishes this project's soft-delete convention (database-conventions.md: "bir gerçek DELETE
-- SQL komutu hiç kullanılmaz") for the one entity that actually exercises deletion today -
-- AuditListenerTest proves userRepository.deleteById() issues a real DELETE against this table.
-- No other core entity has delete functionality yet, so deleted_at isn't added speculatively
-- elsewhere - the same pattern applies whenever a future entity actually needs deletion.
ALTER TABLE "user" ADD COLUMN deleted_at TIMESTAMPTZ;
