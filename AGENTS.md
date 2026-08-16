# Agent instructions

Keep the library generic and Java 21 compatible. Public behavior is tested at TestNG-facing seams; no JUnit small-test API belongs in published modules. Never log unredacted headers or response bodies. Medium tests may use only localhost and Testcontainers.
