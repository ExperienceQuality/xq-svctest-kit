# XQ Service Test Kit

`xq-svctest-kit` is the generic XQ Java medium-test foundation. It provides explicit localhost HTTP assertions, provider-side OpenAPI response checks, safe failure diagnostics, and disposable PostgreSQL fixtures for TestNG. It deliberately does not model a production service, discover or deploy environments, or depend on the service SDK.

- **Medium test**: a TestNG test against only a local HTTP fixture and/or Testcontainers resource.
- **Provider assertion**: checks that a provider's observed HTTP response is described by its OpenAPI document.
- **Diagnostic**: failure context that redacts configured sensitive values before being emitted.
