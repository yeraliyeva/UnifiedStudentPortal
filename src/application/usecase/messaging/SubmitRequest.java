package application.usecase.messaging;

import application.Result;
import domain.enums.HelpType;
import domain.enums.UrgencyLevel;
import domain.messaging.Request;
import domain.repository.RequestRepository;
import domain.shared.IdSequence;
import domain.user.User;
import infrastructure.logging.Logger;

public final class SubmitRequest {
    private final RequestRepository requests;
    private final IdSequence ids;
    private final Logger logger;

    public SubmitRequest(RequestRepository requests, IdSequence ids, Logger logger) {
        this.requests = requests;
        this.ids = ids;
        this.logger = logger;
    }

    public Result execute(User user, String title, HelpType type, UrgencyLevel urgency, String additionalInfo) {
        Request r = new Request(ids.next(), user.username(), title, type, user.faculty(), urgency, additionalInfo);
        requests.save(r);
        logger.log(user.username(), "Submitted request: " + type + " — " + title);
        return Result.ok("Request submitted: " + r);
    }
}
