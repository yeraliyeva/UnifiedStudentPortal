package infrastructure.persistence.inmemory;

import domain.messaging.Notification;
import domain.repository.NotificationRepository;
import domain.shared.Username;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryNotificationRepository implements NotificationRepository {
    private final List<Notification> store = new ArrayList<>();

    @Override public void save(Notification n) { store.add(n); }
    @Override public List<Notification> findFor(Username recipient) {
        return store.stream().filter(n -> n.recipient().equals(recipient)).toList();
    }
    @Override public void clearFor(Username recipient) {
        store.removeIf(n -> n.recipient().equals(recipient));
    }
}
