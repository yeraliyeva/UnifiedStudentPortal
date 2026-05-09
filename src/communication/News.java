package communication;

import java.time.LocalDate;

/**
 * University news item.
 */
public class News {
    private static int idCounter = 1;

    private final int id;
    private String title;
    private String body;
    private final String authorUsername;
    private final LocalDate postedDate;
    private final java.util.List<String> comments = new java.util.ArrayList<>();

    public News(String title, String body, String authorUsername) {
        this.id = idCounter++;
        this.title = title;
        this.body = body;
        this.authorUsername = authorUsername;
        this.postedDate = LocalDate.now();
    }

    public void addComment(String comment) { comments.add(comment); }
    public void edit(String newTitle, String newBody) { this.title = newTitle; this.body = newBody; }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getAuthorUsername() { return authorUsername; }
    public LocalDate getPostedDate() { return postedDate; }
    public java.util.List<String> getComments() { return java.util.Collections.unmodifiableList(comments); }

    @Override
    public String toString() {
        return "[NEWS-" + id + "] " + title + " (by " + authorUsername + ", " + postedDate + ")";
    }
}
