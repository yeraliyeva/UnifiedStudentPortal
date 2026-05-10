package presentation.rest.auth;

import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.User;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;
import presentation.rest.routing.Route;
import presentation.rest.routing.RouteHandler;

import java.util.Optional;

/** Validates the Bearer token on every protected request and enforces role-based access control. */
public final class SecurityFilter {
    private final TokenStore tokens;
    private final UserRepository users;

    public SecurityFilter(TokenStore tokens, UserRepository users) {
        this.tokens = tokens;
        this.users  = users;
    }

    /** Wraps a handler with auth checks; returns 401/403 if the token is missing or the role doesn't match. */
    public RouteHandler guard(Route route, RouteHandler handler) {
        if (route.isPublic()) return handler;

        return request -> {
            Optional<User> user = resolveUser(request);
            if (user.isEmpty()) return HttpResponse.unauthorized();

            User authenticated = user.get();
            if (!route.requiredRole().isInstance(authenticated)) return HttpResponse.forbidden();

            RequestContext.set(authenticated);
            try {
                return handler.handle(request);
            } finally {
                RequestContext.clear();
            }
        };
    }


    private Optional<User> resolveUser(HttpRequest request) {
        return request.header("Authorization")
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7).trim())
                .flatMap(tokens::resolve)
                .flatMap(username -> users.findByUsername(username));
    }
}
