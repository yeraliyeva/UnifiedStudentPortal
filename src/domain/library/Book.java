package domain.library;

import domain.shared.Username;

public final class Book {
    private final BookId id;
    private final String title;
    private final String author;
    private Username borrowedBy;

    public Book(BookId id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public BookId id() { return id; }
    public String title() { return title; }
    public String author() { return author; }
    public boolean isBorrowed() { return borrowedBy != null; }
    public java.util.Optional<Username> borrower() { return java.util.Optional.ofNullable(borrowedBy); }

    public void lendTo(Username user) {
        if (isBorrowed()) throw new IllegalStateException("already borrowed");
        this.borrowedBy = user;
    }
    public void returnFrom(Username user) {
        if (!user.equals(borrowedBy)) throw new IllegalStateException("not the borrower");
        this.borrowedBy = null;
    }

    @Override public String toString() {
        return "[" + id + "] \"" + title + "\" by " + author + (isBorrowed() ? " [BORROWED]" : " [AVAILABLE]");
    }
}
