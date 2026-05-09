package domain.repository;

import domain.messaging.News;

import java.util.List;
import java.util.Optional;

public interface NewsRepository {
    void save(News news);
    Optional<News> findById(int id);
    List<News> findAllSorted();
    void delete(int id);
}
