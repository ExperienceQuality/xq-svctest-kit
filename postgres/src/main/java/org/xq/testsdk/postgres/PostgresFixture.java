package org.xq.testsdk.postgres;

import org.testcontainers.containers.PostgreSQLContainer;

/** A disposable PostgreSQL fixture for a hermetic TestNG medium test. */
public final class PostgresFixture implements AutoCloseable {
    private final PostgreSQLContainer<?> container;

    private PostgresFixture(PostgreSQLContainer<?> container) { this.container = container; }

    public static PostgresFixture start() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine");
        container.start();
        return new PostgresFixture(container);
    }

    public String jdbcUrl() { return container.getJdbcUrl(); }
    public String username() { return container.getUsername(); }
    public String password() { return container.getPassword(); }
    @Override public void close() { container.stop(); }
}
