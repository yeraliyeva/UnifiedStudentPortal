package infrastructure.persistence.inmemory;

import domain.library.Book;
import domain.library.BookId;
import domain.repository.BookRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryBookRepository implements BookRepository {
    private final Map<BookId, Book> store = new LinkedHashMap<>();

    @Override public void save(Book book) { store.put(book.id(), book); }
    @Override public Optional<Book> findById(BookId id) { return Optional.ofNullable(store.get(id)); }
    @Override public Optional<Book> findFirstAvailableByTitle(String title) {
        return store.values().stream()
                .filter(b -> b.title().equalsIgnoreCase(title) && !b.isBorrowed())
                .findFirst();
    }
    @Override public Collection<Book> findAll() { return Collections.unmodifiableCollection(store.values()); }
    @Override public void delete(BookId id) { store.remove(id); }
}
