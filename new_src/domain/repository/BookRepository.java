package domain.repository;

import domain.library.Book;
import domain.library.BookId;

import java.util.Collection;
import java.util.Optional;

public interface BookRepository {
    void save(Book book);
    Optional<Book> findById(BookId id);
    Optional<Book> findFirstAvailableByTitle(String title);
    Collection<Book> findAll();
    void delete(BookId id);
}
