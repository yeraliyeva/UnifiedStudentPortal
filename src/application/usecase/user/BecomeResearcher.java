package application.usecase.user;

import application.Result;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class BecomeResearcher {
    private final Logger logger;

    public BecomeResearcher(Logger logger) { this.logger = logger; }

    public Result execute(User user, String field) {
        if (!(user instanceof ResearcherCapable rc)) {
            return Result.fail("This role cannot become a researcher.");
        }
        if (rc.isResearcher()) {
            return Result.fail("Already a researcher.");
        }
        rc.activateResearcher(field);
        logger.log(user.username(), "Activated researcher capability (" + field + ")");
        return Result.ok("Researcher activated for field: " + field);
    }
}
