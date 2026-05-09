package infrastructure.persistence.inmemory;

import domain.logging.LogEntry;
import domain.repository.LogRepository;
import domain.shared.Username;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InMemoryLogRepository implements LogRepository {
    private final List<LogEntry> entries = new ArrayList<>();

    @Override public void append(LogEntry entry) { entries.add(entry); }
    @Override public List<LogEntry> findAll() { return Collections.unmodifiableList(entries); }
    @Override public List<LogEntry> findByActor(Username actor) {
        return entries.stream().filter(e -> e.actor().equals(actor)).toList();
    }
}
