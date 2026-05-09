package application.usecase.library;

import application.Result;
import domain.library.Book;
import domain.repository.BookRepository;
import domain.user.User;
import infrastructure.logging.Logger;

public final class ReturnBook {
    private final BookRepository books;
    private final Logger logger;

    public ReturnBook(BookRepository books, Logger logger) {
        this.books = books;
        this.logger = logger;
    }

    public Result execute(User user, String title) {
        Book book = books.findAll().stream()
                .filter(b -> b.title().equalsIgnoreCase(title)
                        && b.borrower().map(u -> u.equals(user.username())).orElse(false))
                .findFirst().orElse(null);
        if (book == null) return Result.fail("You did not borrow that book.");
        book.returnFrom(user.username());
        books.save(book);
        logger.log(user.username(), "Returned book: " + book.title());
        return Result.ok("Returned: " + book.title());
    }
}
