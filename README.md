# XQ Service Test Kit

Generic Java 21 / TestNG tooling for hermetic **medium** service tests. Version 0.1 supports localhost HTTP assertions, provider-side OpenAPI response checks, redacted diagnostics, bounded polling, and PostgreSQL Testcontainers fixtures. Large tests are intentionally out of scope.

## Modules

| Module | Purpose |
| --- | --- |
| `org.xq:xq-test-sdk-jvm-core` | diagnostics, polling, and correlation helpers |
| `org.xq:xq-test-sdk-jvm-http` | explicit localhost HTTP client and response assertions |
| `org.xq:xq-test-sdk-jvm-openapi` | provider response checks against OpenAPI JSON |
| `org.xq:xq-test-sdk-jvm-postgres` | disposable PostgreSQL Testcontainers fixture |
| `org.xq:xq-test-sdk-jvm-bom` | aligned module versions |

Use an immutable version; `latest` is a release mirror and is never a dependency version. See [docs/release-runbook.md](docs/release-runbook.md).
