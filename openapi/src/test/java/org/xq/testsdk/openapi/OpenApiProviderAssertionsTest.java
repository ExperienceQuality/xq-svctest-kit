package org.xq.testsdk.openapi;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.xq.testsdk.http.LocalHttpClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.expectThrows;

public class OpenApiProviderAssertionsTest {
    private HttpServer server;
    private LocalHttpClient client;
    private static final String SPEC = """
        {"openapi":"3.0.3","paths":{"/health":{"get":{"responses":{"200":{"content":{"application/json":{"schema":{"type":"object","required":["status"]}}}}}}}}}
        """;

    @BeforeMethod
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "{\"status\":\"UP\"}".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        client = LocalHttpClient.at("http://localhost:" + server.getAddress().getPort());
    }

    @AfterMethod
    public void stopServer() { server.stop(0); }

    @Test(groups = "xq-svctest-kit-medium")
    public void acceptsResponseDeclaredByProviderOpenApi() {
        OpenApiProviderAssertions.fromJson(SPEC).assertResponse("GET", "/health", client.get("/health"));
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void rejectsAnUndeclaredOperation() {
        expectThrows(AssertionError.class,
            () -> OpenApiProviderAssertions.fromJson(SPEC).assertResponse("GET", "/missing", client.get("/health")));
    }

    @Test(groups = "xq-svctest-kit-medium")
    public void rejectsAResponseWithAnInvalidOpenApiPropertyType() {
        String typedSpec = SPEC.replace("\"status\"]", "\"status\"],\"properties\":{\"status\":{\"type\":\"integer\"}}");
        expectThrows(AssertionError.class,
            () -> OpenApiProviderAssertions.fromJson(typedSpec).assertResponse("GET", "/health", client.get("/health")));
    }
}
