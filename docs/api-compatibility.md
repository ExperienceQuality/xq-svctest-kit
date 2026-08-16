# API compatibility

Before an immutable release, compare every published module JAR with the prior immutable GitHub Packages version using `japicmp`. Store the HTML/XML report with the release evidence and fail the release on binary-incompatible public changes unless the owner has approved the associated major-version decision. The first prerelease establishes the baseline; it still publishes its generated public API manifest below.
