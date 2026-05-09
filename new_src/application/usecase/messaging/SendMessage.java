package application.usecase.messaging;

import application.Result;
import domain.enums.UrgencyLevel;
import domain.messaging.Message;
import domain.repository.MessageRepository;
import domain.repository.UserRepository;
import domain.shared.IdSequence;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class SendMessage {
    private final MessageRepository messages;
    private final UserRepository users;
    private final IdSequence ids;
    private final Logger logger;

    public SendMessage(MessageRepository messages, UserRepository users, IdSequence ids, Logger logger) {
        this.messages = messages;
        this.users = users;
        this.ids = ids;
        this.logger = logger;
    }

    public Result execute(Username sender, Username recipient, String subject, String body, UrgencyLevel urgency) {
        if (!users.exists(recipient)) return Result.fail("Recipient not found: " + recipient);
        Message msg = new Message(ids.next(), sender, recipient, subject, body, urgency);
        messages.save(msg);
        logger.log(sender, "Sent message to " + recipient);
        return Result.ok("Message sent.");
    }
}
