package presentation.rest.routing;

/**
 * Immutable value object describing a single registered route.
 *
 * @param method       the HTTP method (GET, POST, etc.)
 * @param pattern      the URL pattern, e.g. "/api/courses/{id}/enroll"
 * @param handler      the handler invoked when this route matches
 * @param requiredRole the domain class the authenticated user must be an instance of,
 *                     or {@code null} for public (unauthenticated) endpoints
 */
public record Route(
        HttpMethod method,
        String pattern,
        RouteHandler handler,
        Class<?> requiredRole
) {
    /** Public route — no authentication required. */
    public static Route of(HttpMethod method, String pattern, RouteHandler handler) {
        return new Route(method, pattern, handler, null);
    }

    /** Protected route — caller must be an instance of {@code requiredRole}. */
    public static Route of(HttpMethod method, String pattern, RouteHandler handler, Class<?> requiredRole) {
        return new Route(method, pattern, handler, requiredRole);
    }

    /** Returns true if the given HTTP method and path match this route's pattern. */
    public boolean matches(String httpMethod, String path) {
        if (!this.method.name().equalsIgnoreCase(httpMethod)) return false;
        String[] patternParts = pattern.split("/");
        String[] pathParts    = path.split("/");
        if (patternParts.length != pathParts.length) return false;
        for (int i = 0; i < patternParts.length; i++) {
            // {placeholder} matches any segment
            if (!patternParts[i].startsWith("{") && !patternParts[i].equals(pathParts[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isPublic() { return requiredRole == null; }
}
