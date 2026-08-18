package org.xq.testsdk.http;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class LocalHttpClientTest {
    private HttpServer server;
    private LocalHttpClient client;

    @BeforeMethod
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "{\"token\":\"secret-value\",\"status\":\"UP\"}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/stalled", exchange -> {
            try {
                Thread.sleep(Duration.ofSeconds(2));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });
        server.start();
        client = LocalHttpClient.at("http://localhost:" + server.getAddress().getPort());
    }

    @AfterMethod
    public void stopServer() { server.stop(0); }

    @Test(groups = "xq-svctest-kit-medium")
    public void getsLocalResponseAndExposesCorrelationId() {
        LocalHttpResponse response = client.get("/health");
        response.assertStatus(200).assertBodyContains("UP");
        assertEquals(response.correlationId().orElse(null), null);
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void postsJsonToALocalFixture() {
        LocalHttpResponse response = client.postJson("/health", "{\"key\":\"demo\"}");
        response.assertStatus(200);
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void redactsResponseBodiesInFailureDiagnostics() {
        AssertionError error = expectThrows(AssertionError.class,
            () -> client.get("/health").assertStatus(201));
        assertEquals(error.getMessage().contains("secret-value"), false);
        assertEquals(error.getMessage().contains("[REDACTED]"), true);
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void neverIncludesABodyInFailureDiagnosticsByDefault() {
        AssertionError error = expectThrows(AssertionError.class, () -> client.get("/health").assertStatus(201));
        assertEquals(error.getMessage().contains("secret-value"), false);
        assertEquals(error.getMessage().contains("[REDACTED]"), true);
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void rejectsAbsoluteExternalRequestUris() {
        IllegalArgumentException error = expectThrows(IllegalArgumentException.class,
            () -> client.get("https://example.com/api?token=secret-value"));
        assertEquals(error.getMessage().contains("example.com"), true);
        assertEquals(error.getMessage().contains("secret-value"), false);
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void rejectsAuthorityRelativeExternalRequestUris() {
        IllegalArgumentException error = expectThrows(IllegalArgumentException.class,
            () -> client.get("//example.com/api?token=secret-value"));
        assertEquals(error.getMessage().contains("example.com"), true);
        assertEquals(error.getMessage().contains("secret-value"), false);
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void boundsStalledLocalResponsesAndRedactsRequestCredentialsAndQuery() {
        LocalHttpClient shortTimeoutClient = LocalHttpClient.at(
            "http://user:secret-value@localhost:" + server.getAddress().getPort(),
            Duration.ofSeconds(1), Duration.ofMillis(100));

        AssertionError error = expectThrows(AssertionError.class,
            () -> shortTimeoutClient.get("/stalled?token=secret-value"));

        assertEquals(error.getMessage().contains("secret-value"), false);
        assertEquals(error.getMessage().contains("token="), false);
        assertEquals(error.getMessage().contains("/stalled"), true);
        assertEquals(error.getCause().getMessage().contains("secret-value"), false);
    }
}
