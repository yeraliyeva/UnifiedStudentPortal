import common.DataSeeder;
import common.Login;
import users.User;

/**
 * Application entry point.
 *
 * Flow:
 * 1. Seed demo data
 * 2. Show login prompt
 * 3. Route authenticated user to their role-specific menu
 * 4. On log out, return to login
 */
public class Main {

    public static void main(String[] args) {
        DataSeeder.seed();

        while (true) {
            User user = Login.authenticate();
            if (user == null) {
                System.out.println("Exiting system.");
                break;
            }
            user.showMenu();
            // After showMenu returns (user logged out), loop back to login
            System.out.println("\nLogged out. Returning to login...\n");
        }
    }
}
