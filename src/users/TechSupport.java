package users;

import communication.Order;
import data.Database;
import enums.Faculty;
import enums.Gender;
import enums.OrderStatus;

import java.time.LocalDate;

/**
 * Technical Support Specialist.
 * Views, accepts/rejects, and completes tech orders.
 */
public class TechSupport extends Employee {

    public TechSupport(String firstName, String lastName, String username,
                       String password, Gender gender, LocalDate dateOfBirth,
                       String email, Faculty faculty, double salary,
                       LocalDate hireDate, String insuranceNumber) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
    }

    public void viewNewOrders() {
        System.out.println("\n=== NEW ORDERS ===");
        Database.getInstance().getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW)
                .forEach(System.out::println);
    }

    public void viewMyOrders() {
        System.out.println("\n=== MY ORDERS ===");
        Database.getInstance().getAllOrders().stream()
                .filter(o -> getUsername().equals(o.getExecutorUsername()))
                .forEach(System.out::println);
    }

    public void processOrderInteractive() {
        viewNewOrders();
        System.out.print("Enter Order ID: ");
        int id = readInt();
        Database.getInstance().findOrderById(id).ifPresentOrElse(order -> {
            System.out.println("1. Accept   2. Reject");
            int choice = readInt();
            if (choice == 1) {
                order.accept(getUsername());
                System.out.println("Order accepted.");
            } else if (choice == 2) {
                order.reject();
                System.out.println("Order rejected / returned to queue.");
            }
        }, () -> System.out.println("Order not found."));
    }

    public void completeOrderInteractive() {
        viewMyOrders();
        System.out.print("Enter Order ID to mark done: ");
        int id = readInt();
        Database.getInstance().findOrderById(id).ifPresentOrElse(order -> {
            order.markDone();
            System.out.println("Order marked as done.");
        }, () -> System.out.println("Order not found."));
    }

    public void fixSystem() {
        System.out.println("Running system diagnostics... [simulated]");
        System.out.println("All systems operational.");
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== TECH SUPPORT MENU ===
                    1. View new orders
                    2. Process an order (accept/reject)
                    3. View my orders
                    4. Complete an order
                    5. Fix system (diagnostics)
                    6. View inbox
                    7. Send message
                    8. View news
                    9. Personal info
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1 -> viewNewOrders();
                case 2 -> processOrderInteractive();
                case 3 -> viewMyOrders();
                case 4 -> completeOrderInteractive();
                case 5 -> fixSystem();
                case 6 -> viewInbox();
                case 7 -> sendMessageInteractive();
                case 8 -> viewNews();
                case 9 -> viewPersonalInfo();
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
