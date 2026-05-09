package infrastructure.persistence.inmemory;

import domain.messaging.News;
import domain.repository.NewsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InMemoryNewsRepository implements NewsRepository {
    private final List<News> store = new ArrayList<>();

    @Override public void save(News news) {
        store.removeIf(n -> n.id() == news.id());
        store.add(news);
    }
    @Override public Optional<News> findById(int id) {
        return store.stream().filter(n -> n.id() == id).findFirst();
    }
    @Override public List<News> findAllSorted() {
        return store.stream().sorted().toList();
    }
    @Override public void delete(int id) { store.removeIf(n -> n.id() == id); }
}
