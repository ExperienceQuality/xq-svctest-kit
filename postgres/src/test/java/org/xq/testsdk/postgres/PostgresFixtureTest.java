package org.xq.testsdk.postgres;

import java.sql.DriverManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class PostgresFixtureTest {
    @Test(groups = "xq-svctest-kit-medium")
    public void startsDisposablePostgresAndProvidesJdbcUrl() throws Exception {
        try (PostgresFixture fixture = PostgresFixture.start()) {
            try (var connection = DriverManager.getConnection(fixture.jdbcUrl(), fixture.username(), fixture.password());
                 var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT 1")) {
                result.next();
                assertEquals(result.getInt(1), 1);
            }
        }
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void cleansUpContainerWhenStartupFails() {
        FailingPostgresContainer container = new FailingPostgresContainer();

        assertThrows(IllegalStateException.class, () -> PostgresFixture.start(() -> container));

        assertTrue(container.startAttempted);
        assertTrue(container.stopAttempted);
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void makesConnectionDetailsUnavailableAfterClose() {
        TrackingPostgresContainer container = new TrackingPostgresContainer();
        PostgresFixture fixture = PostgresFixture.start(() -> container);
        fixture.close();

        assertEquals(container.stopCalls, 1);
        assertThrows(IllegalStateException.class, fixture::jdbcUrl);
        assertThrows(IllegalStateException.class, fixture::username);
        assertThrows(IllegalStateException.class, fixture::password);

        fixture.close();
        assertEquals(container.stopCalls, 1);
    }

    private static final class FailingPostgresContainer
            extends PostgreSQLContainer<FailingPostgresContainer> {
        private boolean startAttempted;
        private boolean stopAttempted;

        private FailingPostgresContainer() {
            super(PostgresFixture.POSTGRES_IMAGE);
        }

        @Override
        public void start() {
            startAttempted = true;
            throw new IllegalStateException("simulated container startup failure");
        }

        @Override
        public void stop() {
            stopAttempted = true;
        }
    }

    private static final class TrackingPostgresContainer
            extends PostgreSQLContainer<TrackingPostgresContainer> {
        private int stopCalls;

        private TrackingPostgresContainer() {
            super(PostgresFixture.POSTGRES_IMAGE);
        }

        @Override
        public void start() {
            // The lifecycle seam is unit verified without creating a second Docker fixture.
        }

        @Override
        public void stop() {
            stopCalls++;
        }
    }
}
