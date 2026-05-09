package presentation.cli.menu;

import application.Result;
import application.usecase.user.BecomeResearcher;
import domain.repository.UserRepository;
import domain.user.User;
import presentation.cli.Console;

public final class BecomeResearcherAction {
    private final Console console;
    private final BecomeResearcher useCase;
    private final UserRepository users;

    public BecomeResearcherAction(Console console, BecomeResearcher useCase, UserRepository users) {
        this.console = console;
        this.useCase = useCase;
        this.users = users;
    }

    public void run(User user) {
        String field = console.readLine("Research field:");
        Result r = useCase.execute(user, field);
        console.println(r.message());
        if (r.success()) users.save(user);
    }
}
