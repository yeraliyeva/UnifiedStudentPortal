package presentation.rest.auth;

import domain.shared.Username;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe in-memory session registry that maps UUID tokens to usernames. */
public final class TokenStore {
    private final Map<String, Username> sessions = new ConcurrentHashMap<>();

    /** Generates a new UUID session token for the given user and stores it. */
    public String createSession(Username username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return token;
    }

    /** Returns the username associated with the token, or empty if the token is invalid. */
    public Optional<Username> resolve(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return Optional.ofNullable(sessions.get(token));
    }

    /** Removes the session token, logging the user out. */
    public void invalidate(String token) {
        sessions.remove(token);
    }
}
