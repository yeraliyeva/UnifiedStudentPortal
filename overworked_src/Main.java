import common.DataSeeder;
import common.Login;
import users.User;

public class Main {
    public static void main(String[] args) {
        // 1. Seed demo data (users, courses, etc.)
        DataSeeder.seed();

        boolean systemRunning = true;

        while (systemRunning) {
            // 2. Authenticate
            User currentUser = Login.authenticate();

            if (currentUser == null) {
                systemRunning = false;
                continue;
            }

            // 3. Enter role-specific menu
            currentUser.showMenu();
        }

        System.out.println("System terminated. Goodbye!");
    }
}
