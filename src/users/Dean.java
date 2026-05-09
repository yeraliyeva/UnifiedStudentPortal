package users;

import communication.Request;
import data.Database;
import enums.Faculty;
import enums.Gender;
import enums.RequestStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Dean of a faculty. Extends Teacher.
 * Receives and processes incoming requests from managers (DEANS_OFFICE routing).
 */
public class Dean extends Teacher {

    public Dean(String firstName, String lastName, String username,
                String password, Gender gender, LocalDate dateOfBirth,
                String email, Faculty faculty, double salary,
                LocalDate hireDate, String insuranceNumber, String degree) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber, degree, true);
    }

    public void viewIncomingRequests() {
        System.out.println("\n=== INCOMING REQUESTS (Dean: " + getFaculty() + ") ===");
        Database.getInstance().getAllRequests().stream()
                .filter(r -> r.getFaculty() == getFaculty()
                        && r.getStatus() == RequestStatus.ACCEPTED)
                .forEach(System.out::println);
    }

    public void processRequestInteractive() {
        viewIncomingRequests();
        System.out.print("Enter Request ID: ");
        int id = readInt();
        Database.getInstance().findRequestById(id).ifPresentOrElse(req -> {
            System.out.println("1. Approve   2. Reject");
            int choice = readInt();
            if (choice == 1) {
                req.setStatus(RequestStatus.APPROVED);
                System.out.println("Request approved.");
                // notify requester
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Approved",
                        "Your request has been approved by Dean " + getUsername() + ".",
                        enums.UrgencyLevel.MEDIUM);
            } else if (choice == 2) {
                req.setStatus(RequestStatus.NOT_APPROVED);
                System.out.println("Request rejected.");
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Not Approved",
                        "Your request was not approved. Contact the dean's office for details.",
                        enums.UrgencyLevel.MEDIUM);
            }
        }, () -> System.out.println("Request not found."));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== DEAN MENU ===
                    1. View incoming requests
                    2. Process a request
                    3. View news
                    4. View inbox
                    5. Send message
                    6. Research cabinet
                    7. View taught courses
                    8. Personal info
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1 -> viewIncomingRequests();
                case 2 -> processRequestInteractive();
                case 3 -> viewNews();
                case 4 -> viewInbox();
                case 5 -> sendMessageInteractive();
                case 6 -> viewResearchCabinet();
                case 7 -> viewTaughtCourses();
                case 8 -> viewPersonalInfo();
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
