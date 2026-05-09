package application.usecase.admin;

import application.Result;
import domain.repository.UserRepository;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class DeleteUser {
    private final UserRepository users;
    private final Logger logger;

    public DeleteUser(UserRepository users, Logger logger) {
        this.users = users;
        this.logger = logger;
    }

    public Result execute(Username actor, Username target) {
        if (!users.exists(target)) return Result.fail("User not found.");
        users.delete(target);
        logger.log(actor, "Deleted user: " + target);
        return Result.ok("User deleted.");
    }
}
