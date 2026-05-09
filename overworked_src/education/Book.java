package education;

import java.util.Objects;

/**
 * Represents a library book.
 */
public class Book {
    private static int idCounter = 1;

    private final int bookId;
    private final String title;
    private final String author;
    private boolean borrowed;

    public Book(String title, String author) {
        this.bookId = idCounter++;
        this.title = title;
        this.author = author;
        this.borrowed = false;
    }

    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isBorrowed() { return borrowed; }
    public void setBorrowed(boolean borrowed) { this.borrowed = borrowed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return bookId == book.bookId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return "[BOOK-" + bookId + "] \"" + title + "\" by " + author + (borrowed ? " [BORROWED]" : " [AVAILABLE]");
    }
}
