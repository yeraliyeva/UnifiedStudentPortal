package domain.messaging;

import domain.shared.Username;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class News implements Comparable<News> {
    private final int id;
    private String title;
    private String body;
    private final Username author;
    private final LocalDateTime publishedAt;
    private final List<String> comments = new ArrayList<>();
    private boolean pinned;

    public News(int id, String title, String body, Username author) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.author = author;
        this.publishedAt = LocalDateTime.now();
        this.pinned = title != null && title.toLowerCase().startsWith("research");
    }

    public int id() { return id; }
    public String title() { return title; }
    public String body() { return body; }
    public Username author() { return author; }
    public LocalDateTime publishedAt() { return publishedAt; }
    public List<String> comments() { return Collections.unmodifiableList(comments); }
    public boolean isPinned() { return pinned; }

    public void edit(String newTitle, String newBody) { this.title = newTitle; this.body = newBody; }
    public void addComment(String comment) { comments.add(comment); }

    @Override public int compareTo(News o) {
        if (this.pinned != o.pinned) return this.pinned ? -1 : 1;
        return o.publishedAt.compareTo(this.publishedAt);
    }

    @Override public String toString() {
        return "[NEWS-" + id + "]" + (pinned ? " [PINNED]" : "") + " " + title + " — by " + author;
    }
}
