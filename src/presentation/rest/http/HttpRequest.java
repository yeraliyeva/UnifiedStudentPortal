package presentation.rest.http;

import com.sun.net.httpserver.HttpExchange;
import infrastructure.persistence.json.JsonReader;
import infrastructure.persistence.json.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * Immutable value object wrapping an inbound HTTP exchange.
 * Parsed once at the router entry point; controllers receive this clean object.
 */
public final class HttpRequest {
    private final String method;
    private final String path;
    private final String rawBody;
    private final HttpExchange exchange;
    private final String[] segments;

    private HttpRequest(String method, String path, String rawBody, HttpExchange exchange) {
        this.method   = method;
        this.path     = path;
        this.rawBody  = rawBody;
        this.exchange = exchange;
        // "/api/courses/CS101/enroll" -> ["", "api", "courses", "CS101", "enroll"]
        this.segments = path.split("/");
    }

    public static HttpRequest from(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return new HttpRequest(ex.getRequestMethod(), ex.getRequestURI().getPath(), body, ex);
    }

    /** Returns the parsed JSON body, or an empty object if the body is blank or not JSON. */
    public JsonValue.JsonObject body() {
        if (rawBody == null || rawBody.isBlank()) return JsonValue.obj();
        try {
            JsonValue parsed = JsonReader.parse(rawBody);
            return (parsed instanceof JsonValue.JsonObject obj) ? obj : JsonValue.obj();
        } catch (Exception e) {
            return JsonValue.obj();
        }
    }

    /**
     * Returns a path segment by index (0-based, skipping the leading empty string).
     * e.g. for "/api/courses/CS101", segment(0)="api", segment(1)="courses", segment(2)="CS101"
     */
    public Optional<String> pathSegment(int index) {
        int adjusted = index + 1; // skip leading ""
        return (adjusted < segments.length) ? Optional.of(segments[adjusted]) : Optional.empty();
    }

    public Optional<String> header(String name) {
        var headers = exchange.getRequestHeaders().get(name);
        return (headers != null && !headers.isEmpty()) ? Optional.of(headers.get(0)) : Optional.empty();
    }

    public String method() { return method; }
    public String path()   { return path;   }
    public String rawBody(){ return rawBody; }

    /** Exposes the underlying exchange only to HttpResponse for writing back. */
    HttpExchange exchange() { return exchange; }
}
