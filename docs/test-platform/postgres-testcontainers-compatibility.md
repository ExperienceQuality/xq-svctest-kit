# PostgreSQL Testcontainers compatibility evidence

This record supports XQ Hub work item #7. It captures the Docker-backed
medium-test fixture baseline and the lifecycle coverage added to the PostgreSQL
fixture.

## Compatibility matrix

| Component | Tested identity |
| --- | --- |
| Application baseline | `14b20ccc0704c44d9e68b2bef7a3e213a170ce7b` (`codex/svc-test-kit-0.1-readiness`) |
| Java | OpenJDK 21.0.10 (Homebrew, 2026-01-20) |
| Gradle | 8.12.1 (`gradle/wrapper/gradle-wrapper.properties`) |
| Testcontainers PostgreSQL module | 1.21.4 |
| Docker Engine server | 29.2.1, `linux/amd64` |
| Test process architecture | `x86_64` (forced with `arch -x86_64`) |
| PostgreSQL image | `postgres@sha256:cf78e76683b9ca8c5733cbbdce6c9262b45b6767934dd0a95e671f9a0fc20685` |
| PostgreSQL image metadata | OCI index for `postgres:16-alpine`; resolved image version `16.15-alpine3.24` |

The fixture uses the OCI index digest rather than the mutable
`postgres:16-alpine` tag. Docker selects the platform-specific manifest from
that immutable index; the recorded execution selected `linux/amd64`.

## Validation command

All runs used a locally available Colima socket. A `clean` was run before each
recorded command so the Testcontainers fixture test executed rather than being
reported as up to date.

```bash
DOCKER_HOST=unix:///Users/automation2/.colima/default/docker.sock \
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock \
arch -x86_64 ./gradlew check cyclonedxBom --no-daemon --no-configuration-cache --console=plain
```

| Evidence | Source | Result | Wall time | Executed PostgreSQL tests | Repeated-run count |
| --- | --- | --- | --- | --- | --- |
| Baseline | `14b20ccc0704c44d9e68b2bef7a3e213a170ce7b` | Pass | 18.99s | 1 | 1 |
| Lifecycle change | `pending commit` | Pass | 16.79s, 16.76s, 16.65s | 3 per run | 3 / 3 pass |

The baseline verifies a disposable PostgreSQL connection. The lifecycle change
retains that Docker-backed check and adds deterministic coverage that:

1. stops the created container when startup throws; and
2. stops once on close and makes the JDBC URL, username, and password
   unavailable after close.

## Residual risk

This matrix is evidence for Docker Engine 29.2.1 on `linux/amd64` only. The
image index is multi-platform, but `linux/arm64` has not been exercised in this
work item. The Testcontainers suite still requires a reachable Docker daemon
and pulls the pinned image if it is absent locally; failures in either external
dependency remain environment failures rather than fixture lifecycle failures.
