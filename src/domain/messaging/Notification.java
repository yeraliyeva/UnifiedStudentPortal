package domain.messaging;

import domain.shared.Username;

import java.time.LocalDateTime;

public record Notification(Username recipient, String text, LocalDateTime at) {
    public static Notification of(Username recipient, String text) {
        return new Notification(recipient, text, LocalDateTime.now());
    }
    @Override public String toString() { return "[" + at + "] " + text; }
}
