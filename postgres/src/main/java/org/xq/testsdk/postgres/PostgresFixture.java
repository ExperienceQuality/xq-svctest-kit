package org.xq.testsdk.postgres;

import java.util.Objects;
import java.util.function.Supplier;
import org.testcontainers.containers.PostgreSQLContainer;

/** A disposable PostgreSQL fixture for a hermetic TestNG medium test. */
public final class PostgresFixture implements AutoCloseable {
    /**
     * PostgreSQL 16 Alpine multi-platform OCI index, resolved on 2026-08-18.
     *
     * <p>The digest, rather than the mutable {@code postgres:16-alpine} tag, is
     * deliberately part of the fixture contract so a medium-test run uses the
     * reviewed image identity on every supported Docker architecture.</p>
     */
    static final String POSTGRES_IMAGE =
            "postgres@sha256:cf78e76683b9ca8c5733cbbdce6c9262b45b6767934dd0a95e671f9a0fc20685";

    private final PostgreSQLContainer<?> container;
    private boolean closed;

    private PostgresFixture(PostgreSQLContainer<?> container) { this.container = container; }

    public static PostgresFixture start() {
        return start(() -> new PostgreSQLContainer<>(POSTGRES_IMAGE));
    }

    /** Package-visible seam for lifecycle verification without a Docker failure fixture. */
    static PostgresFixture start(Supplier<? extends PostgreSQLContainer<?>> containerFactory) {
        PostgreSQLContainer<?> container = Objects.requireNonNull(
                Objects.requireNonNull(containerFactory, "containerFactory").get(), "containerFactory result");
        boolean started = false;
        try {
            container.start();
            started = true;
            return new PostgresFixture(container);
        } finally {
            if (!started) {
                container.stop();
            }
        }
    }

    public synchronized String jdbcUrl() { ensureOpen(); return container.getJdbcUrl(); }
    public synchronized String username() { ensureOpen(); return container.getUsername(); }
    public synchronized String password() { ensureOpen(); return container.getPassword(); }

    @Override public synchronized void close() {
        if (!closed) {
            closed = true;
            container.stop();
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("PostgreSQL fixture is closed");
        }
    }
}
