package infrastructure.persistence.orm.repository;

import domain.messaging.Message;
import domain.repository.MessageRepository;
import domain.shared.Username;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.MessageMapper;
import infrastructure.persistence.orm.Repository;

import java.util.List;

public final class OrmMessageRepository implements MessageRepository {
    private final Repository<Message, Integer> repo;

    public OrmMessageRepository(Database db) {
        this.repo = new Repository<>(db, "messages", new MessageMapper());
    }

    @Override public void save(Message m) { repo.save(m); }
    @Override public List<Message> inboxOf(Username recipient) {
        return repo.whereEq("recipient", recipient.value())
                .orderBy(Message::compareTo)
                .list();
    }
}
