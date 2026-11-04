# File/Image Storage

The `settings.branding.logo_url` field in [tenant-model.md](tenant-model.md) implied the existence of a tenant logo, but **where this image would be uploaded** had never been defined — files are not embedded in the database, a separate storage layer is needed.

## Decision: S3-compatible object storage

- Images (the tenant logo, and in the future module-specific needs such as product/menu photos) are uploaded not to the database but to an object storage compatible with the S3 API.
- Which provider (AWS S3, Cloudflare R2, or the object storage of the hosting provider that will be chosen) will be settled together with the decision in [open-questions.md](../open-questions.md) item 8 (hosting) — thanks to S3 API compatibility the code side does not change even if the provider changes.
- Only the file's **URL** is kept in the database (`tenant.settings.branding.logo_url`), not the file itself.

## Upload flow

```
[Frontend] → [API: request a presigned upload URL] → [Frontend uploads the file directly to object storage] → [API: save the URL to tenant.settings]
```

- The file is uploaded directly to object storage without passing through the backend server (the presigned URL method) — the standard practice so as not to keep the backend needlessly busy with file traffic.

## Security rule

- The uploaded file type is restricted (only `image/png`, `image/jpeg`, etc.) — consistent with the input validation principle in [security.md](security.md).
- A file size upper limit is set (suggestion: 2 MB, more than enough for a logo).
- Each tenant's uploaded files are kept under a folder/prefix belonging to its own tenant (separation by `tenant_id`) — so that one tenant cannot access another tenant's file by guessing its file URL.

## Out of scope (not in the MVP)

- Image processing (automatic resizing/thumbnail generation) — a logo is usually a small file and is used as-is in the MVP.
- Video/large file support — out of scope, evaluated separately if a need arises.

## Related files

- [tenant-model.md](tenant-model.md) — the `branding.logo_url` field
- [security.md](security.md)
- [../open-questions.md](../open-questions.md) — the provider choice to be settled together with the hosting decision
