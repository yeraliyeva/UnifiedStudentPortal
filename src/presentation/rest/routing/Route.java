package presentation.rest.routing;

/**
 * A single registered route binding an HTTP method and URL pattern to a handler.
 *
 * @param method       the HTTP method (GET, POST, etc.)
 * @param pattern      the URL pattern, e.g. "/api/courses/{id}/enroll"
 * @param handler      the handler invoked when this route matches
 * @param requiredRole the user class required to access this route, or null for public endpoints
 */
public record Route(
        HttpMethod method,
        String pattern,
        RouteHandler handler,
        Class<?> requiredRole
) {
    /** Creates a public route that requires no authentication. */
    public static Route of(HttpMethod method, String pattern, RouteHandler handler) {
        return new Route(method, pattern, handler, null);
    }

    /** Creates a protected route that requires the caller to be an instance of requiredRole. */
    public static Route of(HttpMethod method, String pattern, RouteHandler handler, Class<?> requiredRole) {
        return new Route(method, pattern, handler, requiredRole);
    }

    /** Returns true if this route matches the given HTTP method and request path. */
    public boolean matches(String httpMethod, String path) {
        if (!this.method.name().equalsIgnoreCase(httpMethod)) return false;
        String[] patternParts = pattern.split("/");
        String[] pathParts    = path.split("/");
        if (patternParts.length != pathParts.length) return false;
        for (int i = 0; i < patternParts.length; i++) {
            if (!patternParts[i].startsWith("{") && !patternParts[i].equals(pathParts[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isPublic() { return requiredRole == null; }
}
