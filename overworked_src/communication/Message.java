package communication;

import enums.MessageStatus;
import enums.UrgencyLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Internal message between users.
 * Implements Comparable for sorting by urgency then date.
 */
public class Message implements Comparable<Message> {
    private static int idCounter = 1;

    private final int id;
    private final String sender;
    private final String recipient;
    private final String subject;
    private final String body;
    private MessageStatus status;
    private final UrgencyLevel urgency;
    private final LocalDateTime sentAt;

    public Message(String sender, String recipient, String subject, String body, UrgencyLevel urgency) {
        this.id = idCounter++;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.urgency = urgency;
        this.status = MessageStatus.UNREAD;
        this.sentAt = LocalDateTime.now();
    }

    public void markRead() { this.status = MessageStatus.READ; }

    public int getId() { return id; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public MessageStatus getStatus() { return status; }
    public UrgencyLevel getUrgency() { return urgency; }
    public LocalDateTime getSentAt() { return sentAt; }

    @Override
    public int compareTo(Message other) {
        // Higher urgency first
        int urgencyCompare = other.urgency.ordinal() - this.urgency.ordinal();
        if (urgencyCompare != 0) return urgencyCompare;
        return this.sentAt.compareTo(other.sentAt);
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("[MSG-%d] [%s] From: %s | Subject: %s | %s | %s",
                id, urgency, sender, subject, status, sentAt.format(fmt));
    }
}
