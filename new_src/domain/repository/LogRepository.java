package domain.repository;

import domain.logging.LogEntry;
import domain.shared.Username;

import java.util.List;

public interface LogRepository {
    void append(LogEntry entry);
    List<LogEntry> findAll();
    List<LogEntry> findByActor(Username actor);
}
