package domain.repository;

import domain.messaging.Notification;
import domain.shared.Username;

import java.util.List;

public interface NotificationRepository {
    void save(Notification notification);
    List<Notification> findFor(Username recipient);
    void clearFor(Username recipient);
}
