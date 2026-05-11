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

/** Matches inbound requests to registered routes and dispatches them through the security filter. */
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
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept-Language");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        HttpRequest request = HttpRequest.from(exchange);

        String langHeader = request.header("Accept-Language").orElse("en").toLowerCase();
        domain.enums.Language lang = domain.enums.Language.ENGLISH;
        if (langHeader.startsWith("ru")) lang = domain.enums.Language.RUSSIAN;
        else if (langHeader.startsWith("kz") || langHeader.startsWith("kk")) lang = domain.enums.Language.KAZAKH;
        infrastructure.i18n.PropertiesTranslator.INSTANCE.switchTo(lang);

        HttpResponse response = dispatch(request);
        response.send(exchange);
    }

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
