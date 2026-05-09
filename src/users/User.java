package users;

import communication.Message;
import communication.Request;
import data.Database;
import enums.Faculty;
import enums.Gender;
import enums.HelpType;
import enums.UrgencyLevel;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstract base for all system users.
 *
 * Holds identity, credentials, and shared communication behaviour
 * (send/view messages, submit requests, view news).
 *
 * Subclasses override showMenu() to provide role-specific options.
 */
public abstract class User {

    // ── identity ──────────────────────────────────────────────────
    private String firstName;
    private String lastName;
    private String username;         // unique login key
    private String password;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String email;
    private Faculty faculty;

    protected static final java.util.Scanner scanner = common.AppScanner.get();

    protected User(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.faculty = faculty;
    }

    // ── abstract ──────────────────────────────────────────────────

    /** Each role shows its own menu. */
    public abstract void showMenu();

    // ── messaging ─────────────────────────────────────────────────

    public void sendMessage(String recipientUsername, String subject, String body, UrgencyLevel urgency) {
        Database db = Database.getInstance();
        if (!db.userExists(recipientUsername)) {
            System.out.println("User '" + recipientUsername + "' not found.");
            return;
        }
        Message msg = new Message(username, recipientUsername, subject, body, urgency);
        db.addMessage(msg);
        System.out.println("Message sent to " + recipientUsername + ".");
    }

    public void viewInbox() {
        List<Message> inbox = Database.getInstance().getMessagesFor(username);
        if (inbox.isEmpty()) {
            System.out.println("Your inbox is empty.");
            return;
        }
        System.out.println("\n=== INBOX (sorted by urgency) ===");
        for (int i = 0; i < inbox.size(); i++) {
            System.out.println(i + ". " + inbox.get(i));
        }
        System.out.print("Enter message number to read, or -1 to go back: ");
        int choice = readInt();
        if (choice >= 0 && choice < inbox.size()) {
            Message msg = inbox.get(choice);
            msg.markRead();
            System.out.println("\nFrom   : " + msg.getSender());
            System.out.println("Subject: " + msg.getSubject());
            System.out.println("Body   : " + msg.getBody());
        }
    }

    public void sendMessageInteractive() {
        System.out.print("Recipient username: ");
        String to = scanner.nextLine().trim();
        System.out.print("Subject: ");
        String subject = scanner.nextLine().trim();
        System.out.print("Body: ");
        String body = scanner.nextLine().trim();
        System.out.println("Urgency (LOW / MEDIUM / HIGH): ");
        UrgencyLevel urgency = parseUrgency(scanner.nextLine().trim());
        sendMessage(to, subject, body, urgency);
    }

    // ── requests ──────────────────────────────────────────────────

    public void submitRequest(HelpType type, UrgencyLevel urgency, String additionalInfo) {
        Request req = new Request(username, type, faculty, urgency, additionalInfo);
        Database.getInstance().addRequest(req);
        System.out.println("Request submitted: " + req);
    }

    public void viewMyRequests() {
        System.out.println("\n=== MY REQUESTS ===");
        Database.getInstance().getAllRequests().stream()
                .filter(r -> r.getRequesterUsername().equals(username))
                .forEach(System.out::println);
    }

    // ── news ──────────────────────────────────────────────────────

    public void viewNews() {
        System.out.println("\n=== UNIVERSITY NEWS ===");
        var newsList = Database.getInstance().getAllNews();
        if (newsList.isEmpty()) { System.out.println("No news available."); return; }
        newsList.forEach(System.out::println);
    }

    public void commentOnNews() {
        viewNews();
        System.out.print("Enter News ID to comment on: ");
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            System.out.print("Your comment: ");
            String comment = scanner.nextLine().trim();
            news.addComment(username + ": " + comment);
            System.out.println("Comment added.");
        }, () -> System.out.println("News not found."));
    }

    // ── personal data ─────────────────────────────────────────────

    public void viewPersonalInfo() {
        System.out.println("\n=== PERSONAL INFO ===");
        System.out.println("Name    : " + firstName + " " + lastName);
        System.out.println("Username: " + username);
        System.out.println("Email   : " + email);
        System.out.println("Gender  : " + gender);
        System.out.println("DOB     : " + dateOfBirth);
        System.out.println("Faculty : " + faculty);
    }

    public void editPersonalInfo() {
        System.out.println("1. Change email  2. Change password  0. Back");
        int choice = readInt();
        switch (choice) {
            case 1 -> {
                System.out.print("New email: ");
                this.email = scanner.nextLine().trim();
                System.out.println("Email updated.");
            }
            case 2 -> {
                System.out.print("New password: ");
                this.password = scanner.nextLine().trim();
                System.out.println("Password updated.");
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────

    protected int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        } catch (java.util.NoSuchElementException e) {
            return -2; // stream ended
        }
    }

    protected UrgencyLevel parseUrgency(String s) {
        return switch (s.toUpperCase()) {
            case "HIGH" -> UrgencyLevel.HIGH;
            case "LOW"  -> UrgencyLevel.LOW;
            default     -> UrgencyLevel.MEDIUM;
        };
    }

    // ── getters / setters ─────────────────────────────────────────

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Gender getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Faculty getFaculty() { return faculty; }

    public void setFirstName(String v) { this.firstName = v; }
    public void setLastName(String v) { this.lastName = v; }
    public void setPassword(String v) { this.password = v; }
    public void setEmail(String v) { this.email = v; }
    public void setGender(Gender v) { this.gender = v; }
    public void setFaculty(Faculty v) { this.faculty = v; }

    public boolean checkPassword(String pwd) { return password.equals(pwd); }

    @Override
    public String toString() {
        return username + " (" + firstName + " " + lastName + ") [" + getClass().getSimpleName() + "]";
    }
}
