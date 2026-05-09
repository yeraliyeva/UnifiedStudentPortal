package application.usecase.library;

import application.Result;
import domain.library.Book;
import domain.repository.BookRepository;
import domain.user.BookBorrowerCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class BorrowBook {
    private final BookRepository books;
    private final Logger logger;

    public BorrowBook(BookRepository books, Logger logger) {
        this.books = books;
        this.logger = logger;
    }

    public Result execute(User user, String title) {
        if (!(user instanceof BookBorrowerCapable)) return Result.fail("This role cannot borrow books.");
        Book book = books.findFirstAvailableByTitle(title).orElse(null);
        if (book == null) return Result.fail("Book is not available.");
        book.lendTo(user.username());
        books.save(book);
        logger.log(user.username(), "Borrowed book: " + book.title());
        return Result.ok("Borrowed: " + book.title());
    }
}
