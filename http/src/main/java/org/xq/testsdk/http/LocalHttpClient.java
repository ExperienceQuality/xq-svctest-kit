package org.xq.testsdk.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

/** An explicit HTTP client for a local test fixture; it does not discover environments. */
public final class LocalHttpClient {
    private final URI baseUri;
    private final HttpClient client;

    private LocalHttpClient(URI baseUri, HttpClient client) {
        this.baseUri = baseUri;
        this.client = client;
    }

    public static LocalHttpClient at(String baseUrl) {
        URI uri = URI.create(baseUrl);
        String host = uri.getHost();
        if (!"localhost".equals(host) && !"127.0.0.1".equals(host) && !"::1".equals(host)) {
            throw new IllegalArgumentException("only localhost endpoints are supported: " + baseUrl);
        }
        return new LocalHttpClient(uri, HttpClient.newHttpClient());
    }

    public LocalHttpResponse get(String path) {
        return exchange(path, "GET", null);
    }

    public LocalHttpResponse postJson(String path, String json) {
        Objects.requireNonNull(json, "json");
        return exchange(path, "POST", json);
    }

    private LocalHttpResponse exchange(String path, String method, String json) {
        Objects.requireNonNull(path, "path");
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(baseUri.resolve(path));
            if (json == null) {
                request.GET();
            } else {
                request.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json));
            }
            HttpResponse<String> response = client.send(request.build(),
                HttpResponse.BodyHandlers.ofString());
            return new LocalHttpResponse(response.statusCode(), response.headers().map(), response.body());
        } catch (Exception exception) {
            throw new AssertionError("localhost " + method + " request failed: " + baseUri.resolve(path), exception);
        }
    }
}
