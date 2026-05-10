package presentation.rest.auth;

import domain.shared.Username;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session registry.
 *
 * <p>Generates a UUID token per login session and maps it back to the owning
 * {@link Username}. Thread-safe via {@link ConcurrentHashMap}.
 *
 * <p>GRASP: Information Expert — this class owns all session state and is
 * the sole authority on token validity.
 */
public final class TokenStore {
    private final Map<String, Username> sessions = new ConcurrentHashMap<>();

    /** Creates a fresh session token for the given user and returns it. */
    public String createSession(Username username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return token;
    }

    /** Returns the {@link Username} associated with the token, or empty if invalid/expired. */
    public Optional<Username> resolve(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return Optional.ofNullable(sessions.get(token));
    }

    /** Removes the session, effectively logging the user out. */
    public void invalidate(String token) {
        sessions.remove(token);
    }
}
