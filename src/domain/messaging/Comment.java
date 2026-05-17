package domain.messaging;

import domain.shared.Username;

import java.time.LocalDateTime;

public final class Comment {
    private final Username author;
    private final String text;
    private final LocalDateTime postedAt;

    public Comment(Username author, String text, LocalDateTime postedAt) {
        this.author = author;
        this.text = text;
        this.postedAt = postedAt;
    }

    public static Comment now(Username author, String text) {
        return new Comment(author, text, LocalDateTime.now());
    }

    public Username author() { return author; }
    public String text() { return text; }
    public LocalDateTime postedAt() { return postedAt; }
}
