package org.xq.testsdk.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A response with assertion helpers that redact configured values in failures. */
public final class LocalHttpResponse {
    private final int status;
    private final Map<String, List<String>> headers;
    private final String body;
    LocalHttpResponse(int status, Map<String, List<String>> headers, String body) {
        this.status = status;
        this.headers = Map.copyOf(headers);
        this.body = body;
    }

    public int status() { return status; }
    public String body() { return body; }
    public Optional<String> header(String name) {
        return headers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(name))
            .flatMap(entry -> entry.getValue().stream()).findFirst();
    }
    public Optional<String> correlationId() {
        return header("x-correlation-id");
    }
    public LocalHttpResponse assertStatus(int expected) {
        if (status != expected) {
            throw new AssertionError("expected HTTP status " + expected + " but was " + status + "; body=" + redactedBody());
        }
        return this;
    }
    public LocalHttpResponse assertBodyContains(String expected) {
        if (!body.contains(expected)) {
            throw new AssertionError("response body did not contain expected content; body=" + redactedBody());
        }
        return this;
    }
    private String redactedBody() { return body.isEmpty() ? body : "[REDACTED]"; }
}
