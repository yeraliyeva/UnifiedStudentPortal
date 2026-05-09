package application.usecase.research;

import application.Result;
import domain.repository.UserRepository;
import domain.service.SubscriptionService;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class UnsubscribeFromJournal {
    private final SubscriptionService subscriptions;
    private final UserRepository users;
    private final Logger logger;

    public UnsubscribeFromJournal(SubscriptionService subscriptions, UserRepository users, Logger logger) {
        this.subscriptions = subscriptions;
        this.users = users;
        this.logger = logger;
    }

    public Result execute(User user, String journal) {
        if (!(user instanceof ResearcherCapable rc) || !rc.isResearcher()) {
            return Result.fail("You must become a researcher first.");
        }
        subscriptions.unsubscribe(rc, journal);
        users.save(user);
        logger.log(user.username(), "Unsubscribed from journal: " + journal);
        return Result.ok("Unsubscribed.");
    }
}
