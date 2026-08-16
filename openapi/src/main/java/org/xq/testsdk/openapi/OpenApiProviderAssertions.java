package org.xq.testsdk.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xq.testsdk.http.LocalHttpResponse;

/** Minimal provider-side assertions for OpenAPI JSON responses observed over localhost HTTP. */
public final class OpenApiProviderAssertions {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final JsonNode document;

    private OpenApiProviderAssertions(JsonNode document) { this.document = document; }

    public static OpenApiProviderAssertions fromJson(String document) {
        try {
            return new OpenApiProviderAssertions(JSON.readTree(document));
        } catch (Exception exception) {
            throw new IllegalArgumentException("OpenAPI document must be valid JSON", exception);
        }
    }

    public void assertResponse(String method, String path, LocalHttpResponse response) {
        JsonNode operation = document.path("paths").path(path).path(method.toLowerCase());
        if (operation.isMissingNode()) {
            throw new AssertionError("OpenAPI does not declare " + method + " " + path);
        }
        JsonNode contract = operation.path("responses").path(String.valueOf(response.status()));
        if (contract.isMissingNode()) {
            throw new AssertionError("OpenAPI does not declare HTTP " + response.status() + " for " + method + " " + path);
        }
        String contentType = response.header("content-type").orElse("").split(";", 2)[0];
        JsonNode media = contract.path("content").path(contentType);
        if (media.isMissingNode()) {
            throw new AssertionError("OpenAPI does not declare response media type " + contentType);
        }
        try {
            JsonNode body = JSON.readTree(response.body());
            assertSchema(media.path("schema"), body, "$response");
        } catch (AssertionError exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AssertionError("response is not valid JSON required by OpenAPI", exception);
        }
    }

    private void assertSchema(JsonNode schema, JsonNode value, String path) {
        String type = schema.path("type").asText("");
        if (!type.isEmpty() && !matches(type, value)) {
            throw new AssertionError(path + " must be " + type);
        }
        if ("object".equals(type)) {
            for (JsonNode required : schema.path("required")) {
                if (!value.has(required.asText())) {
                    throw new AssertionError(path + " lacks required OpenAPI property " + required.asText());
                }
            }
            schema.path("properties").fields().forEachRemaining(entry -> {
                if (value.has(entry.getKey())) { assertSchema(entry.getValue(), value.path(entry.getKey()), path + "." + entry.getKey()); }
            });
        }
        if ("array".equals(type)) {
            for (JsonNode item : value) { assertSchema(schema.path("items"), item, path + "[]"); }
        }
    }

    private boolean matches(String type, JsonNode value) {
        return switch (type) {
            case "object" -> value.isObject(); case "array" -> value.isArray(); case "string" -> value.isTextual();
            case "integer" -> value.isIntegralNumber(); case "number" -> value.isNumber(); case "boolean" -> value.isBoolean();
            default -> true;
        };
    }
}
