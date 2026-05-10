package presentation.rest.routing;

import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;

/**
 * Strategy interface for a single route handler.
 * Each controller method implements this interface, keeping controllers
 * composable and independently testable.
 */
@FunctionalInterface
public interface RouteHandler {
    HttpResponse handle(HttpRequest request);
}
