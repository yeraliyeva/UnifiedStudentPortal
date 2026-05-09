package infrastructure.logging;

import domain.shared.Username;

public interface Logger {
    void log(Username actor, String action);
}
