package domain.repository;

import domain.messaging.Message;
import domain.shared.Username;

import java.util.List;

public interface MessageRepository {
    void save(Message message);
    List<Message> inboxOf(Username recipient);
}
