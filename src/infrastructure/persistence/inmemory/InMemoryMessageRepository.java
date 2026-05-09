package infrastructure.persistence.inmemory;

import domain.messaging.Message;
import domain.repository.MessageRepository;
import domain.shared.Username;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryMessageRepository implements MessageRepository {
    private final List<Message> messages = new ArrayList<>();

    @Override public void save(Message m) { messages.add(m); }
    @Override public List<Message> inboxOf(Username recipient) {
        return messages.stream()
                .filter(m -> m.recipient().equals(recipient))
                .sorted()
                .toList();
    }
}
