package infrastructure.logging;

import domain.logging.LogEntry;
import domain.repository.LogRepository;
import domain.shared.Username;

public final class RepositoryLogger implements Logger {
    private final LogRepository repository;

    public RepositoryLogger(LogRepository repository) { this.repository = repository; }

    @Override public void log(Username actor, String action) {
        repository.append(LogEntry.now(actor, action));
    }
}
