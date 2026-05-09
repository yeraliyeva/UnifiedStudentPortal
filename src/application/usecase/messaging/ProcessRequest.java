package application.usecase.messaging;

import application.Result;
import domain.enums.RequestStatus;
import domain.enums.UrgencyLevel;
import domain.messaging.Message;
import domain.messaging.Request;
import domain.repository.MessageRepository;
import domain.repository.RequestRepository;
import domain.shared.IdSequence;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class ProcessRequest {
    private final RequestRepository requests;
    private final MessageRepository messages;
    private final IdSequence msgIds;
    private final Logger logger;

    public ProcessRequest(RequestRepository requests, MessageRepository messages, IdSequence msgIds, Logger logger) {
        this.requests = requests;
        this.messages = messages;
        this.msgIds = msgIds;
        this.logger = logger;
    }

    public Result execute(Username actor, int requestId, RequestStatus newStatus) {
        Request r = requests.findById(requestId).orElse(null);
        if (r == null) return Result.fail("Request not found.");
        r.changeStatus(newStatus);
        requests.save(r);
        Message reply = new Message(msgIds.next(), actor, r.requester(),
                "Request #" + r.id() + " " + newStatus,
                "Your request has been " + newStatus + " by " + actor + ".",
                UrgencyLevel.MEDIUM);
        messages.save(reply);
        logger.log(actor, "Processed request #" + r.id() + " -> " + newStatus);
        return Result.ok("Request " + newStatus);
    }
}
