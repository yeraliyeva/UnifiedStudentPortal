package communication;

import java.time.LocalDate;

/**
 * University news item.
 * News with topic "Research" are pinned (sorted first).
 */
public class News implements Comparable<News> {
    private static int idCounter = 1;

    private final int id;
    private String title;
    private String body;
    private final String authorUsername;
    private final LocalDate postedDate;
    private final java.util.List<String> comments = new java.util.ArrayList<>();
    private boolean pinned;  // Research news is pinned

    public News(String title, String body, String authorUsername) {
        this.id = idCounter++;
        this.title = title;
        this.body = body;
        this.authorUsername = authorUsername;
        this.postedDate = LocalDate.now();
        // Auto-pin news with "Research" in the title
        this.pinned = title.toLowerCase().contains("research");
    }

    public void addComment(String comment) { comments.add(comment); }
    public void edit(String newTitle, String newBody) {
        this.title = newTitle;
        this.body = newBody;
        this.pinned = newTitle.toLowerCase().contains("research");
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getAuthorUsername() { return authorUsername; }
    public LocalDate getPostedDate() { return postedDate; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public java.util.List<String> getComments() { return java.util.Collections.unmodifiableList(comments); }

    /** Pinned news first, then by date descending */
    @Override
    public int compareTo(News other) {
        if (this.pinned && !other.pinned) return -1;
        if (!this.pinned && other.pinned) return 1;
        return other.postedDate.compareTo(this.postedDate);
    }

    @Override
    public String toString() {
        String pin = pinned ? " 📌" : "";
        return "[NEWS-" + id + "]" + pin + " " + title + " (by " + authorUsername + ", " + postedDate + ")";
    }
}
