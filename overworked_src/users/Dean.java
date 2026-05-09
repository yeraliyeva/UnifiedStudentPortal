package users;

import common.Messages;
import communication.Request;
import data.Database;
import enums.Faculty;
import enums.Gender;
import enums.RequestStatus;
import enums.UrgencyLevel;

import java.time.LocalDate;

public class Dean extends Teacher {

    public Dean(String firstName, String lastName, String username,
                String password, Gender gender, LocalDate dateOfBirth,
                String email, Faculty faculty, double salary,
                LocalDate hireDate, String insuranceNumber, String degree) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber, degree, true);
    }

    public void viewIncomingRequests() {
        System.out.println(Messages.fmt("dean.requests.title", getFaculty()));
        Database.getInstance().getAllRequests().stream()
                .filter(r -> r.getFaculty() == getFaculty() && r.getStatus() == RequestStatus.ACCEPTED)
                .forEach(System.out::println);
    }

    public void processRequestInteractive() {
        viewIncomingRequests();
        System.out.print(Messages.get("dean.request.enter_id"));
        int id = readInt();
        Database.getInstance().findRequestById(id).ifPresentOrElse(req -> {
            System.out.println(Messages.get("dean.request.action"));
            int choice = readInt();
            if (choice == 1) {
                req.setStatus(RequestStatus.APPROVED);
                System.out.println(Messages.get("dean.request.approved"));
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Approved",
                        Messages.fmt("dean.request.approve_msg", getUsername()),
                        UrgencyLevel.MEDIUM);
            } else if (choice == 2) {
                req.setStatus(RequestStatus.NOT_APPROVED);
                System.out.println(Messages.get("dean.request.rejected"));
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Not Approved",
                        Messages.get("dean.request.reject_msg"),
                        UrgencyLevel.MEDIUM);
            }
        }, () -> System.out.println(Messages.get("dean.request.not_found")));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("dean.menu.title"));
            System.out.println("1. " + Messages.get("dean.menu.1"));
            System.out.println("2. " + Messages.get("dean.menu.2"));
            System.out.println("3. " + Messages.get("dean.menu.3"));
            System.out.println("4. " + Messages.get("dean.menu.4"));
            System.out.println("5. " + Messages.get("dean.menu.5"));
            System.out.println("6. " + Messages.get("dean.menu.6"));
            System.out.println("7. " + Messages.get("dean.menu.7"));
            System.out.println("8. " + Messages.get("dean.menu.8"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            switch (readInt()) {
                case 1  -> viewIncomingRequests();
                case 2  -> processRequestInteractive();
                case 3  -> viewNews();
                case 4  -> viewInbox();
                case 5  -> sendMessageInteractive();
                case 6  -> viewResearchCabinet();
                case 7  -> viewTaughtCourses();
                case 8  -> { viewPersonalInfo(); editPersonalInfo(); }
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }
}
