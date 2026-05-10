package presentation.rest.auth;

import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.User;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;
import presentation.rest.routing.Route;
import presentation.rest.routing.RouteHandler;

import java.util.Optional;

/**
 * Security middleware that intercepts every protected request.
 *
 * <p>Responsibilities (SRP):
 * <ol>
 *   <li>Extract and validate the Bearer token from the Authorization header.</li>
 *   <li>Resolve the token to a domain {@link User} via {@link TokenStore} + {@link UserRepository}.</li>
 *   <li>Check that the user's runtime type satisfies the route's required role.</li>
 *   <li>Populate {@link RequestContext} so the downstream controller can call {@code RequestContext.current()}.</li>
 * </ol>
 *
 * <p>OCP: new roles are handled automatically via {@code instanceof} — no changes needed here.
 */
public final class SecurityFilter {
    private final TokenStore tokens;
    private final UserRepository users;

    public SecurityFilter(TokenStore tokens, UserRepository users) {
        this.tokens = tokens;
        this.users  = users;
    }

    /**
     * Wraps a route handler with authentication + authorisation checks.
     *
     * @param route   the matched route (contains the required role)
     * @param handler the actual controller handler to invoke on success
     * @return a guarded handler
     */
    public RouteHandler guard(Route route, RouteHandler handler) {
        if (route.isPublic()) return handler; // no guard needed

        return request -> {
            Optional<User> user = resolveUser(request);
            if (user.isEmpty()) return HttpResponse.unauthorized();

            User authenticated = user.get();
            if (!route.requiredRole().isInstance(authenticated)) return HttpResponse.forbidden();

            RequestContext.set(authenticated);
            try {
                return handler.handle(request);
            } finally {
                RequestContext.clear(); // always clean up thread-local
            }
        };
    }

    // ── Private ──────────────────────────────────────────────

    private Optional<User> resolveUser(HttpRequest request) {
        return request.header("Authorization")
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7).trim())
                .flatMap(tokens::resolve)
                .flatMap(username -> users.findByUsername(username));
    }
}
