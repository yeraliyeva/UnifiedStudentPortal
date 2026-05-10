package presentation.rest.controller;

import bootstrap.AppContext;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.auth.RequestContext;
import presentation.rest.auth.TokenStore;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;
import presentation.rest.http.HttpStatus;

import java.util.Optional;

/**
 * Handles authentication endpoints: login and logout.
 *
 * <p>GRASP Controller: designated handler for the "Login" and "Logout" system operations.
 */
public final class AuthController {
    private final AppContext ctx;
    private final TokenStore tokens;

    public AuthController(AppContext ctx, TokenStore tokens) {
        this.ctx    = ctx;
        this.tokens = tokens;
    }

    /**
     * POST /api/login
     * Body: { "username": "...", "password": "..." }
     */
    public HttpResponse login(HttpRequest request) {
        JsonValue.JsonObject body = request.body();
        String username = getString(body, "username");
        String password = getString(body, "password");

        if (username.isBlank() || password.isBlank()) {
            return HttpResponse.badRequest("Fields 'username' and 'password' are required.");
        }

        Optional<User> user = ctx.auth.authenticate(username, password);
        if (user.isEmpty()) {
            return HttpResponse.fail(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        String token = tokens.createSession(user.get().username());
        JsonValue responseBody = JsonObjectBuilder.create()
                .put("token", token)
                .put("username", user.get().username().value())
                .put("role",  user.get().getClass().getSimpleName())
                .build();
        return HttpResponse.ok(responseBody);
    }

    /**
     * POST /api/logout
     * Header: Authorization: Bearer {token}
     */
    public HttpResponse logout(HttpRequest request) {
        request.header("Authorization")
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7).trim())
                .ifPresent(tokens::invalidate);
        return HttpResponse.ok(JsonObjectBuilder.create().put("message", "Logged out.").build());
    }

    // ── Helpers ──────────────────────────────────────────────

    private static String getString(JsonValue.JsonObject body, String key) {
        JsonValue val = body.fields().get(key);
        return (val instanceof JsonValue.JsonString s) ? s.value() : "";
    }
}
