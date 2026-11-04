# Package Naming

On the Spring Boot side, Java package names mirror the folder structure one to one — you should be able to guess where a file is from its package name.

| Package | Contents | Layer |
|---|---|---|
| `com.saas.platform.tenants` | Tenant provisioning, activating/deactivating (platform_owner only) | platform |
| `com.saas.platform.billing` | The subscription revenue the platform receives from tenants | platform |
| `com.saas.platform.admin` | Platform admin panel API | platform |
| `com.saas.core.auth` | Authentication, sessions, roles/permissions | core |
| `com.saas.core.tenant` | Company/tenant record, isolation, feature flags | core |
| `com.saas.core.membership` | Membership packages, freezing, renewal | core |
| `com.saas.core.billing` | Invoices/payments the tenant issues to its own customers | core |
| `com.saas.core.notifications` | SMS/WhatsApp/email notification abstraction | core |
| `com.saas.modules.fitness.checkin` | Check-in/check-out control, token validation | fitness |
| `com.saas.modules.fitness.qr` | QR generation, renewal, reading API | fitness |
| `com.saas.modules.fitness.training` | PT appointment calendar | fitness |
| `com.saas.modules.berber.booking` | Appointment type, service duration | berber |
| `com.saas.modules.berber.service` | Service/price definitions | berber |

> `com.saas` is a placeholder — once the real company/domain name is settled it will be changed in all packages at once (see the open questions at the end of the file).

## Rule

- When adding a new module, the package name is always in the format `com.saas.modules.<module_name>.<sub_area>`.
- Only the `platform_owner` role can access `platform.*` packages — see [platform/roles-and-access.md](platform/roles-and-access.md)
- No module other than `core` can import another module's package, nor the `platform` package.
- Shared types (DTOs, enums) are defined in `core`, and modules consume them from there.
- `core.billing` and `platform.billing` are not mixed — for what each one is, see [platform/billing-model.md](platform/billing-model.md)
