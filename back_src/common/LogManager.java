package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory action logger.
 * Requirement: Admin "See log files about user actions"
 */
public class LogManager {

    private static final LogManager INSTANCE = new LogManager();
    private final List<String> logs = new ArrayList<>();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LogManager() {}

    public static LogManager getInstance() { return INSTANCE; }

    public void log(String username, String action) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + username + " — " + action;
        logs.add(entry);
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public List<String> getLogsForUser(String username) {
        return logs.stream()
                .filter(l -> l.contains(username))
                .toList();
    }

    public void printAllLogs() {
        if (logs.isEmpty()) {
            System.out.println("No logs recorded.");
            return;
        }
        System.out.println("\n=== SYSTEM LOGS ===");
        logs.forEach(System.out::println);
    }

    public void printLogsForUser(String username) {
        var userLogs = getLogsForUser(username);
        if (userLogs.isEmpty()) {
            System.out.println("No logs for user: " + username);
            return;
        }
        System.out.println("\n=== LOGS FOR " + username + " ===");
        userLogs.forEach(System.out::println);
    }
}
