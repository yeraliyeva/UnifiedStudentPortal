package application.usecase.research;

import application.Result;
import domain.repository.UserRepository;
import domain.service.SubscriptionService;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class SubscribeToJournal {
    private final SubscriptionService subscriptions;
    private final UserRepository users;
    private final Logger logger;

    public SubscribeToJournal(SubscriptionService subscriptions, UserRepository users, Logger logger) {
        this.subscriptions = subscriptions;
        this.users = users;
        this.logger = logger;
    }

    public Result execute(User user, String journal) {
        if (!(user instanceof ResearcherCapable rc) || !rc.isResearcher()) {
            return Result.fail("You must become a researcher first.");
        }
        boolean ok = subscriptions.subscribe(rc, journal);
        if (!ok) return Result.fail("Journal/project not found: " + journal);
        users.save(user);
        logger.log(user.username(), "Subscribed to journal: " + journal);
        return Result.ok("Subscribed to: " + journal);
    }
}
