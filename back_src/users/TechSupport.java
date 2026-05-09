package users;

import common.LogManager;
import common.Messages;
import data.Database;
import enums.Faculty;
import enums.Gender;
import enums.OrderStatus;
import enums.UrgencyLevel;

import java.time.LocalDate;

public class TechSupport extends Employee {

    public TechSupport(String firstName, String lastName, String username,
                       String password, Gender gender, LocalDate dateOfBirth,
                       String email, Faculty faculty, double salary,
                       LocalDate hireDate, String insuranceNumber) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
    }

    public void viewNewOrders() {
        System.out.println(Messages.get("tech.orders.new_title"));
        Database.getInstance().getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW)
                .forEach(System.out::println);
    }

    public void viewMyOrders() {
        System.out.println(Messages.get("tech.orders.my_title"));
        Database.getInstance().getAllOrders().stream()
                .filter(o -> (o.getStatus() == OrderStatus.ACCEPTED || o.getStatus() == OrderStatus.DONE)
                        && getUsername().equals(o.getExecutorUsername()))
                .forEach(System.out::println);
    }

    public void processOrderInteractive() {
        viewNewOrders();
        System.out.print(Messages.get("tech.orders.enter_id"));
        int id = readInt();
        Database.getInstance().findOrderById(id).ifPresentOrElse(order -> {
            System.out.println(Messages.get("tech.orders.action"));
            int choice = readInt();
            if (choice == 1) {
                order.accept(getUsername());
                LogManager.getInstance().log(getUsername(), "Accepted order #" + id);
                System.out.println(Messages.get("tech.orders.accepted"));
                sendMessage(order.getRequesterUsername(),
                        "Order #" + order.getId() + " Accepted",
                        "Your tech support order is being processed by " + getUsername() + ".",
                        UrgencyLevel.LOW);
            } else if (choice == 2) {
                order.reject();
                LogManager.getInstance().log(getUsername(), "Rejected order #" + id);
                System.out.println(Messages.get("tech.orders.rejected"));
                sendMessage(order.getRequesterUsername(),
                        "Order #" + order.getId() + " Rejected",
                        "Your tech support order was rejected.",
                        UrgencyLevel.LOW);
            }
        }, () -> System.out.println(Messages.get("tech.orders.not_found")));
    }

    public void completeOrderInteractive() {
        viewMyOrders();
        System.out.print(Messages.get("tech.orders.done_id"));
        int id = readInt();
        Database.getInstance().findOrderById(id).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.ACCEPTED && getUsername().equals(order.getExecutorUsername())) {
                order.markDone();
                LogManager.getInstance().log(getUsername(), "Completed order #" + id);
                System.out.println(Messages.get("tech.orders.done"));
                sendMessage(order.getRequesterUsername(),
                        "Order #" + order.getId() + " Completed",
                        "Your tech support order has been resolved.",
                        UrgencyLevel.LOW);
            } else {
                System.out.println(Messages.get("tech.orders.not_found"));
            }
        }, () -> System.out.println(Messages.get("tech.orders.not_found")));
    }

    public void fixSystem() {
        System.out.println(Messages.get("tech.fix.running"));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        System.out.println(Messages.get("tech.fix.ok"));
        LogManager.getInstance().log(getUsername(), "Ran system diagnostics");
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("tech.menu.title"));
            System.out.println("1. " + Messages.get("tech.menu.1"));
            System.out.println("2. " + Messages.get("tech.menu.2"));
            System.out.println("3. " + Messages.get("tech.menu.3"));
            System.out.println("4. " + Messages.get("tech.menu.4"));
            System.out.println("5. " + Messages.get("tech.menu.5"));
            System.out.println("6. " + Messages.get("tech.menu.6"));
            System.out.println("7. " + Messages.get("tech.menu.7"));
            System.out.println("8. " + Messages.get("tech.menu.8"));
            System.out.println("9. " + Messages.get("tech.menu.9"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            switch (readInt()) {
                case 1  -> viewNewOrders();
                case 2  -> processOrderInteractive();
                case 3  -> viewMyOrders();
                case 4  -> completeOrderInteractive();
                case 5  -> fixSystem();
                case 6  -> viewInbox();
                case 7  -> sendMessageInteractive();
                case 8  -> viewNews();
                case 9  -> { viewPersonalInfo(); editPersonalInfo(); }
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }
}
