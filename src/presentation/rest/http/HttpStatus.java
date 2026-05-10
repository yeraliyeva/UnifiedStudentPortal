package presentation.rest.http;

/** Typed HTTP status codes used across the REST layer. */
public enum HttpStatus {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    HttpStatus(int code) { this.code = code; }

    public int code() { return code; }
}
