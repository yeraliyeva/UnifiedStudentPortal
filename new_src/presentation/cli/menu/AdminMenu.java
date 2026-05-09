package presentation.cli.menu;

import application.Result;
import application.usecase.admin.DeleteUser;
import application.usecase.admin.GenerateTopResearcherNews;
import domain.repository.LogRepository;
import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.Admin;
import presentation.cli.Console;

import java.util.List;

public final class AdminMenu extends Menu {
    private final Admin admin;
    private final UserRepository users;
    private final LogRepository logs;
    private final DeleteUser deleteUser;
    private final GenerateTopResearcherNews topResearcher;

    public AdminMenu(Console console, Admin admin, UserRepository users, LogRepository logs,
                     DeleteUser deleteUser, GenerateTopResearcherNews topResearcher) {
        super(console);
        this.admin = admin;
        this.users = users;
        this.logs = logs;
        this.deleteUser = deleteUser;
        this.topResearcher = topResearcher;
    }

    @Override protected String title() { return "=== ADMIN MENU (" + admin.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        return List.of(
                new MenuItem("View all users", this::listUsers),
                new MenuItem("Delete a user", this::deleteUserInteractive),
                new MenuItem("View all logs", this::listLogs),
                new MenuItem("Generate top researcher news", this::topResearcherNews)
        );
    }

    private void listUsers() {
        users.findAll().forEach(u -> console.println("  " + u));
    }

    private void deleteUserInteractive() {
        String name = console.readLine("Username:");
        Result r = deleteUser.execute(admin.username(), new Username(name));
        console.println(r.message());
    }

    private void listLogs() {
        logs.findAll().forEach(e -> console.println(e.toString()));
    }

    private void topResearcherNews() {
        Result r = topResearcher.execute(admin.username());
        console.println(r.message());
    }
}
