package presentation.rest.http;

import com.sun.net.httpserver.HttpExchange;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.json.JsonWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Outbound HTTP response built via static factories and written to the client by calling send(). */
public final class HttpResponse {
    private final HttpStatus status;
    private final JsonValue body;

    private HttpResponse(HttpStatus status, JsonValue body) {
        this.status = status;
        this.body   = body;
    }

    public static HttpResponse ok(JsonValue body) {
        return new HttpResponse(HttpStatus.OK, body);
    }

    public static HttpResponse created(JsonValue body) {
        return new HttpResponse(HttpStatus.CREATED, body);
    }

    public static HttpResponse noContent() {
        return new HttpResponse(HttpStatus.NO_CONTENT, JsonValue.obj());
    }

    public static HttpResponse fail(HttpStatus status, String message) {
        String translated = infrastructure.i18n.PropertiesTranslator.INSTANCE.get(message);
        JsonValue errorBody = JsonObjectBuilder.create()
                .put("error", translated)
                .build();
        return new HttpResponse(status, errorBody);
    }

    public static HttpResponse badRequest(String message) {
        return fail(HttpStatus.BAD_REQUEST, message);
    }

    public static HttpResponse notFound(String message) {
        return fail(HttpStatus.NOT_FOUND, message);
    }

    public static HttpResponse unauthorized() {
        return fail(HttpStatus.UNAUTHORIZED, "Authentication required.");
    }

    public static HttpResponse forbidden() {
        return fail(HttpStatus.FORBIDDEN, "Insufficient permissions.");
    }


    /** Writes the response status, JSON content-type header, and body to the exchange. */
    public void send(HttpExchange exchange) throws IOException {
        byte[] bytes = JsonWriter.write(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status.code(), bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    public HttpStatus status() { return status; }
}
