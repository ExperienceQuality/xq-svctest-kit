package org.xq.testsdk.postgres;

import java.sql.DriverManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

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
}
