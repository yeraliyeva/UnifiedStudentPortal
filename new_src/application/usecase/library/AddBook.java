package application.usecase.library;

import application.Result;
import domain.library.Book;
import domain.library.BookId;
import domain.repository.BookRepository;
import domain.shared.IdSequence;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class AddBook {
    private final BookRepository books;
    private final IdSequence ids;
    private final Logger logger;

    public AddBook(BookRepository books, IdSequence ids, Logger logger) {
        this.books = books;
        this.ids = ids;
        this.logger = logger;
    }

    public Result execute(Username actor, String title, String author) {
        Book book = new Book(new BookId(ids.next()), title, author);
        books.save(book);
        logger.log(actor, "Added book: " + title);
        return Result.ok("Book added: " + title);
    }
}
