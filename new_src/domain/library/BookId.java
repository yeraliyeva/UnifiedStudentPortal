package domain.library;

public record BookId(int value) {
    public BookId {
        if (value < 1) throw new IllegalArgumentException("bookId");
    }
    @Override public String toString() { return "BOOK-" + value; }
}
