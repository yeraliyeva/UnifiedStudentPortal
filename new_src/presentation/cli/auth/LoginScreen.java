package presentation.cli.auth;

import domain.user.User;
import infrastructure.auth.AuthenticationService;
import presentation.cli.Console;

import java.util.Optional;

public final class LoginScreen {
    private final Console console;
    private final AuthenticationService auth;

    public LoginScreen(Console console, AuthenticationService auth) {
        this.console = console;
        this.auth = auth;
    }

    public Optional<User> run() {
        console.println("\n--- LOGIN ---");
        String username = console.readLine("Username:");
        String password = console.readLine("Password:");
        Optional<User> user = auth.authenticate(username, password);
        if (user.isEmpty()) console.println("Invalid username or password.");
        return user;
    }
}
