# Release and withdrawal runbook

Only protected-`main` GitHub Actions may publish an immutable `org.xq` package. A release must have passing TestNG medium evidence, API compatibility evidence, CycloneDX SBOM, dependency/security scan, release notes, and an immutable tag. After success, move the `latest` mirror pointer.

For an owner-authorized withdrawal: repoint or remove `latest`, record the incident, notify known consumers, fully delete the affected GitHub Packages version, and never reuse its version. Preserve the tag, SBOM, attestations, and notes outside Packages.
