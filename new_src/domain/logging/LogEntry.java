package domain.logging;

import domain.shared.Username;

import java.time.LocalDateTime;

public record LogEntry(LocalDateTime at, Username actor, String action) {
    public static LogEntry now(Username actor, String action) {
        return new LogEntry(LocalDateTime.now(), actor, action);
    }
    @Override public String toString() { return at + " — " + actor + " — " + action; }
}
