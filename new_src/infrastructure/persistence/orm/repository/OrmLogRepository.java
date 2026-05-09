package infrastructure.persistence.orm.repository;

import domain.logging.LogEntry;
import domain.repository.LogRepository;
import domain.shared.Username;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.LogEntryMapper;
import infrastructure.persistence.orm.Repository;

import java.util.List;

public final class OrmLogRepository implements LogRepository {
    private final Repository<LogEntry, String> repo;

    public OrmLogRepository(Database db) {
        this.repo = new Repository<>(db, "logs", new LogEntryMapper());
    }

    @Override public void append(LogEntry entry) { repo.save(entry); }
    @Override public List<LogEntry> findAll() { return repo.findAll(); }
    @Override public List<LogEntry> findByActor(Username actor) {
        return repo.findAllMatching(e -> e.actor().equals(actor));
    }
}
