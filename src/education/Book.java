package education;

/**
 * Represents a library book.
 */
public class Book {
    private final String title;
    private final String author;
    private boolean borrowed;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.borrowed = false;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isBorrowed() { return borrowed; }
    public void setBorrowed(boolean borrowed) { this.borrowed = borrowed; }

    @Override
    public String toString() {
        return "\"" + title + "\" by " + author + (borrowed ? " [BORROWED]" : " [AVAILABLE]");
    }
}
