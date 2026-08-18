package org.xq.testsdk.http;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/** An explicit HTTP client for a local test fixture; it does not discover environments. */
public final class LocalHttpClient {
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final URI baseUri;
    private final HttpClient client;
    private final Duration requestTimeout;

    private LocalHttpClient(URI baseUri, HttpClient client, Duration requestTimeout) {
        this.baseUri = baseUri;
        this.client = client;
        this.requestTimeout = requestTimeout;
    }

    public static LocalHttpClient at(String baseUrl) {
        return at(baseUrl, DEFAULT_CONNECT_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
    }

    static LocalHttpClient at(String baseUrl, Duration connectTimeout, Duration requestTimeout) {
        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid local endpoint URL");
        }
        requireLoopbackHttpUri(uri);
        requirePositiveTimeout(connectTimeout, "connectTimeout");
        requirePositiveTimeout(requestTimeout, "requestTimeout");
        HttpClient client = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        return new LocalHttpClient(uri, client, requestTimeout);
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
        URI requestUri = resolveLoopbackPath(path);
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(requestUri).timeout(requestTimeout);
            if (json == null) {
                request.GET();
            } else {
                request.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json));
            }
            HttpResponse<String> response = client.send(request.build(),
                HttpResponse.BodyHandlers.ofString());
            return new LocalHttpResponse(response.statusCode(), response.headers().map(), response.body());
        } catch (Exception exception) {
            throw requestFailure(method, requestUri, exception);
        }
    }

    private URI resolveLoopbackPath(String path) {
        URI requestUri;
        try {
            requestUri = baseUri.resolve(URI.create(path));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid localhost request path");
        }
        requireLoopbackHttpUri(requestUri);
        return requestUri;
    }

    private static void requireLoopbackHttpUri(URI uri) {
        if (!isHttp(uri) || !isLoopbackHost(uri.getHost())) {
            throw new IllegalArgumentException("only HTTP(S) loopback endpoints are supported: " + redactUri(uri));
        }
    }

    private static boolean isHttp(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
    }

    private static boolean isLoopbackHost(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        try {
            return "localhost".equalsIgnoreCase(host)
                || isLiteralLoopbackAddress(host);
        } catch (UnknownHostException exception) {
            return false;
        }
    }

    private static boolean isLiteralLoopbackAddress(String host) throws UnknownHostException {
        String literalAddress = host.startsWith("[") && host.endsWith("]")
            ? host.substring(1, host.length() - 1)
            : host;
        if (!literalAddress.matches("[0-9a-fA-F:.]+")) {
            return false;
        }
        return InetAddress.getByName(literalAddress).isLoopbackAddress();
    }

    private static void requirePositiveTimeout(Duration timeout, String name) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static AssertionError requestFailure(String method, URI requestUri, Exception exception) {
        AssertionError failure = new AssertionError(
            "localhost " + method + " request failed: " + redactUri(requestUri));
        failure.initCause(new IllegalStateException(exception.getClass().getSimpleName()));
        return failure;
    }

    private static String redactUri(URI uri) {
        try {
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null).toString();
        } catch (Exception exception) {
            return "[REDACTED-URI]";
        }
    }
}
