# Release and withdrawal runbook

## Immutable release procedure

Only the `publish` GitHub Actions workflow may publish an immutable `org.xq`
package. Create a protected release tag only after the change has been approved
and merged to protected `main`.

The workflow enforces these publication gates, in this order:

1. The event must be a protected tag.
2. The tag's resolved commit must be an ancestor of `origin/main`. The workflow
   records both commit hashes in `release-evidence/provenance/release-provenance.txt`.
3. `./gradlew check cyclonedxBom` must pass.
4. The workflow uploads a 90-day `xq-svctest-kit-release-evidence-<run-id>`
   artifact containing the generated CycloneDX JSON/XML SBOM, sanitized TestNG
   JUnit XML, and release provenance. The artifact intentionally excludes raw
   HTML reports and logs because they can contain request diagnostics.

Before creating the tag, the release owner must link the release issue to the
successful workflow artifact, independent human review, release notes, and the
required API-compatibility and dependency/security evidence. API-compatibility
and dependency/security tooling are not currently enforced by this workflow;
they remain explicit release-issue gates pending their separate adoption
decision. The tag must not be created until those required records are present.

After publication succeeds, move the `latest` mirror pointer only if the release
issue records the approved immutable version and evidence links.

## Withdrawal

For an owner-authorized withdrawal: repoint or remove `latest`, record the
incident, notify known consumers, fully delete the affected GitHub Packages
version, and never reuse its version. Preserve the tag, workflow evidence,
attestations, and notes outside Packages.
