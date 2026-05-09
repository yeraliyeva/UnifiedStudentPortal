package domain.messaging;

import domain.enums.MessageStatus;
import domain.enums.UrgencyLevel;
import domain.shared.Username;

import java.time.LocalDateTime;

public final class Message implements Comparable<Message> {
    private final int id;
    private final Username sender;
    private final Username recipient;
    private final String subject;
    private final String body;
    private final UrgencyLevel urgency;
    private final LocalDateTime sentAt;
    private MessageStatus status;

    public Message(int id, Username sender, Username recipient, String subject, String body, UrgencyLevel urgency) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.urgency = urgency;
        this.sentAt = LocalDateTime.now();
        this.status = MessageStatus.UNREAD;
    }

    public int id() { return id; }
    public Username sender() { return sender; }
    public Username recipient() { return recipient; }
    public String subject() { return subject; }
    public String body() { return body; }
    public UrgencyLevel urgency() { return urgency; }
    public LocalDateTime sentAt() { return sentAt; }
    public MessageStatus status() { return status; }

    public void markRead() { this.status = MessageStatus.READ; }

    @Override public int compareTo(Message o) {
        int u = Integer.compare(o.urgency.ordinal(), this.urgency.ordinal());
        return u != 0 ? u : o.sentAt.compareTo(this.sentAt);
    }

    @Override public String toString() {
        return "[" + status + "/" + urgency + "] " + sender + " → " + recipient + " : " + subject;
    }
}
