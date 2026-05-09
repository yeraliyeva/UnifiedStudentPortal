package infrastructure.persistence.orm.repository;

import domain.messaging.Notification;
import domain.repository.NotificationRepository;
import domain.shared.Username;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.mapper.NotificationMapper;
import infrastructure.persistence.orm.Repository;

import java.util.ArrayList;
import java.util.List;

public final class OrmNotificationRepository implements NotificationRepository {
    private final Database db;
    private final NotificationMapper mapper = new NotificationMapper();
    private final Repository<Notification, String> repo;

    public OrmNotificationRepository(Database db) {
        this.db = db;
        this.repo = new Repository<>(db, "notifications", mapper);
    }

    @Override public void save(Notification n) { repo.save(n); }
    @Override public List<Notification> findFor(Username recipient) {
        return repo.findAllMatching(n -> n.recipient().equals(recipient));
    }
    @Override public void clearFor(Username recipient) {
        List<JsonValue> kept = new ArrayList<>();
        for (Notification n : repo.findAll()) {
            if (!n.recipient().equals(recipient)) kept.add(mapper.toJson(n));
        }
        db.writeTable("notifications", kept);
    }
}
