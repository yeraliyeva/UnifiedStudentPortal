package presentation.rest.routing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import presentation.rest.auth.SecurityFilter;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;
import presentation.rest.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Central dispatcher — matches an inbound request to a registered {@link Route}
 * and invokes the guarded handler.
 *
 * <p>Design:
 * <ul>
 *   <li>OCP: Add endpoints by calling {@link #register(Route)}. The Router itself never changes.</li>
 *   <li>SRP: Routing only. Auth is delegated to {@link SecurityFilter}.</li>
 *   <li>Implements {@link HttpHandler} so it can be plugged directly into the Java HttpServer.</li>
 * </ul>
 */
public final class Router implements HttpHandler {
    private final List<Route>   routes = new ArrayList<>();
    private final SecurityFilter security;

    public Router(SecurityFilter security) {
        this.security = security;
    }

    public void register(Route route) {
        routes.add(route);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpRequest request = HttpRequest.from(exchange);
        HttpResponse response = dispatch(request);
        response.send(exchange);
    }

    // ── Private ──────────────────────────────────────────────

    private HttpResponse dispatch(HttpRequest request) {
        for (Route route : routes) {
            if (route.matches(request.method(), request.path())) {
                RouteHandler guarded = security.guard(route, route.handler());
                try {
                    return guarded.handle(request);
                } catch (Exception e) {
                    return HttpResponse.fail(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Unexpected server error: " + e.getMessage()
                    );
                }
            }
        }
        return HttpResponse.notFound("No route found for " + request.method() + " " + request.path());
    }
}
