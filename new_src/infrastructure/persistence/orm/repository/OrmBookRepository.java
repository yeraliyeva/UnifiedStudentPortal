package infrastructure.persistence.orm.repository;

import domain.library.Book;
import domain.library.BookId;
import domain.repository.BookRepository;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.BookMapper;
import infrastructure.persistence.orm.Repository;

import java.util.Collection;
import java.util.Optional;

public final class OrmBookRepository implements BookRepository {
    private final Repository<Book, BookId> repo;

    public OrmBookRepository(Database db) {
        this.repo = new Repository<>(db, "books", new BookMapper());
    }

    @Override public void save(Book book) { repo.save(book); }
    @Override public Optional<Book> findById(BookId id) { return repo.findById(id); }
    @Override public Optional<Book> findFirstAvailableByTitle(String title) {
        return repo.select()
                .whereEq("title", title)
                .whereMatch(b -> !b.isBorrowed())
                .first();
    }
    @Override public Collection<Book> findAll() { return repo.findAll(); }
    @Override public void delete(BookId id) { repo.deleteById(id); }
}
