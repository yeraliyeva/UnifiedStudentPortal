package common;

import data.Database;
import users.User;

/**
 * Handles authentication.
 * Returns the authenticated User, or null on failure.
 */
public class Login {

    private static final java.util.Scanner scanner = AppScanner.get();
    private static final int MAX_ATTEMPTS = 3;

    public static User authenticate() {
        System.out.println(Messages.get("login.title"));

        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            String username, password;
            try {
                System.out.print(Messages.get("login.username"));
                username = scanner.nextLine().trim();
                System.out.print(Messages.get("login.password"));
                password = scanner.nextLine().trim();
            } catch (java.util.NoSuchElementException e) {
                break;
            }

            User user = Database.getInstance().getUser(username);
            if (user != null && user.checkPassword(password)) {
                // Set language for the session
                Messages.setLanguage(user.getLanguage());
                System.out.println(Messages.fmt("login.welcome",
                        user.getFirstName(), user.getLastName(),
                        user.getClass().getSimpleName()));
                return user;
            }

            attempts++;
            System.out.println(Messages.fmt("login.fail", MAX_ATTEMPTS - attempts));
        }

        System.out.println(Messages.get("login.denied"));
        return null;
    }
}
