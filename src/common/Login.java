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
        System.out.println("\n=============================");
        System.out.println("   UNIVERSITY SYSTEM LOGIN   ");
        System.out.println("=============================");

        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            String username, password;
            try {
                System.out.print("Username: ");
                username = scanner.nextLine().trim();
                System.out.print("Password: ");
                password = scanner.nextLine().trim();
            } catch (java.util.NoSuchElementException e) {
                break;
            }

            User user = Database.getInstance().getUser(username);
            if (user != null && user.checkPassword(password)) {
                System.out.println("\nWelcome, " + user.getFirstName() + " " + user.getLastName()
                        + " [" + user.getClass().getSimpleName() + "]");
                return user;
            }

            attempts++;
            System.out.println("Incorrect username or password. Attempts left: " + (MAX_ATTEMPTS - attempts));
        }

        System.out.println("Too many failed attempts. Access denied.");
        return null;
    }
}
