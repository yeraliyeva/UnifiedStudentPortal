package infrastructure.persistence.orm.repository;

import domain.messaging.News;
import domain.repository.NewsRepository;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.NewsMapper;
import infrastructure.persistence.orm.Repository;

import java.util.List;
import java.util.Optional;

public final class OrmNewsRepository implements NewsRepository {
    private final Repository<News, Integer> repo;

    public OrmNewsRepository(Database db) {
        this.repo = new Repository<>(db, "news", new NewsMapper());
    }

    @Override public void save(News news) { repo.save(news); }
    @Override public Optional<News> findById(int id) { return repo.findById(id); }
    @Override public List<News> findAllSorted() { return repo.findAll().stream().sorted().toList(); }
    @Override public void delete(int id) { repo.deleteById(id); }
}
