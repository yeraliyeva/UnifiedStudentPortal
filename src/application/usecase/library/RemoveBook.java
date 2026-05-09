package application.usecase.library;

import application.Result;
import domain.library.Book;
import domain.repository.BookRepository;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class RemoveBook {
    private final BookRepository books;
    private final Logger logger;

    public RemoveBook(BookRepository books, Logger logger) {
        this.books = books;
        this.logger = logger;
    }

    public Result execute(Username actor, String title) {
        Book book = books.findAll().stream()
                .filter(b -> b.title().equalsIgnoreCase(title))
                .findFirst().orElse(null);
        if (book == null) return Result.fail("Book not found.");
        if (book.isBorrowed()) return Result.fail("Cannot remove a borrowed book.");
        books.delete(book.id());
        logger.log(actor, "Removed book: " + title);
        return Result.ok("Book removed.");
    }
}
