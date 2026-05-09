package users;

import common.LogManager;
import common.Messages;
import communication.Message;
import communication.Request;
import data.Database;
import enums.Faculty;
import enums.Gender;
import enums.HelpType;
import enums.Language;
import enums.UrgencyLevel;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base for all system users.
 */
public abstract class User {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String email;
    private Faculty faculty;
    private Language language = Language.ENGLISH;

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

    public abstract void showMenu();

    // ── messaging ─────────────────────────────────────────────────

    public void sendMessage(String recipientUsername, String subject, String body, UrgencyLevel urgency) {
        Database db = Database.getInstance();
        if (!db.userExists(recipientUsername)) {
            System.out.println(Messages.fmt("user.not_found", recipientUsername));
            return;
        }
        Message msg = new Message(username, recipientUsername, subject, body, urgency);
        db.addMessage(msg);
        LogManager.getInstance().log(username, "Sent message to " + recipientUsername);
        System.out.println(Messages.fmt("user.msg.sent", recipientUsername));
    }

    public void viewInbox() {
        List<Message> inbox = Database.getInstance().getMessagesFor(username);
        if (inbox.isEmpty()) {
            System.out.println(Messages.get("user.inbox.empty"));
            return;
        }
        System.out.println(Messages.get("user.inbox.title"));
        for (int i = 0; i < inbox.size(); i++) {
            System.out.println(i + ". " + inbox.get(i));
        }
        System.out.print(Messages.get("user.inbox.read_prompt"));
        int choice = readInt();
        if (choice >= 0 && choice < inbox.size()) {
            Message msg = inbox.get(choice);
            msg.markRead();
            System.out.println(Messages.fmt("user.inbox.from", msg.getSender()));
            System.out.println(Messages.fmt("user.inbox.subject", msg.getSubject()));
            System.out.println(Messages.fmt("user.inbox.body", msg.getBody()));
        }
    }

    public void sendMessageInteractive() {
        System.out.print(Messages.get("user.msg.to"));
        String to = scanner.nextLine().trim();
        System.out.print(Messages.get("user.msg.subject"));
        String subject = scanner.nextLine().trim();
        System.out.print(Messages.get("user.msg.body"));
        String body = scanner.nextLine().trim();
        System.out.print(Messages.get("user.msg.urgency"));
        UrgencyLevel urgency = parseUrgency(scanner.nextLine().trim());
        sendMessage(to, subject, body, urgency);
    }

    // ── requests ──────────────────────────────────────────────────

    public void submitRequest(HelpType type, UrgencyLevel urgency, String additionalInfo) {
        Request req = new Request(username, type, faculty, urgency, additionalInfo);
        Database.getInstance().addRequest(req);
        LogManager.getInstance().log(username, "Submitted request: " + type);
        System.out.println(Messages.fmt("user.request.submitted", req));
    }

    public void viewMyRequests() {
        System.out.println(Messages.get("user.requests.title"));
        Database.getInstance().getAllRequests().stream()
                .filter(r -> r.getRequesterUsername().equals(username))
                .forEach(System.out::println);
    }

    // ── news ──────────────────────────────────────────────────────

    public void viewNews() {
        System.out.println(Messages.get("user.news.title"));
        var newsList = Database.getInstance().getAllNews();
        if (newsList.isEmpty()) { System.out.println(Messages.get("user.news.empty")); return; }
        newsList.forEach(System.out::println);
    }

    public void commentOnNews() {
        viewNews();
        System.out.print(Messages.get("user.news.comment_id"));
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            System.out.print(Messages.get("user.news.comment_input"));
            String comment = scanner.nextLine().trim();
            news.addComment(username + ": " + comment);
            LogManager.getInstance().log(username, "Commented on news #" + id);
            System.out.println(Messages.get("user.news.comment_added"));
        }, () -> System.out.println(Messages.get("user.news.not_found")));
    }

    // ── personal data ─────────────────────────────────────────────

    public void viewPersonalInfo() {
        System.out.println(Messages.get("user.info.title"));
        System.out.println(Messages.fmt("user.info.name", firstName, lastName));
        System.out.println(Messages.fmt("user.info.username", username));
        System.out.println(Messages.fmt("user.info.email", email));
        System.out.println(Messages.fmt("user.info.gender", gender));
        System.out.println(Messages.fmt("user.info.dob", dateOfBirth));
        System.out.println(Messages.fmt("user.info.faculty", faculty));
        System.out.println(Messages.fmt("user.info.language", language));
    }

    public void editPersonalInfo() {
        System.out.println(Messages.get("user.edit.menu"));
        int choice = readInt();
        switch (choice) {
            case 1 -> {
                System.out.print(Messages.get("user.edit.email"));
                this.email = scanner.nextLine().trim();
                LogManager.getInstance().log(username, "Changed email");
                System.out.println(Messages.get("user.edit.email.done"));
            }
            case 2 -> {
                System.out.print(Messages.get("user.edit.password"));
                this.password = scanner.nextLine().trim();
                LogManager.getInstance().log(username, "Changed password");
                System.out.println(Messages.get("user.edit.password.done"));
            }
            case 3 -> {
                System.out.println(Messages.get("user.edit.lang_select"));
                int lang = readInt();
                this.language = switch (lang) {
                    case 2 -> Language.KAZAKH;
                    case 3 -> Language.RUSSIAN;
                    default -> Language.ENGLISH;
                };
                Messages.setLanguage(this.language);
                LogManager.getInstance().log(username, "Changed language to " + language);
                System.out.println(Messages.fmt("user.edit.lang.done", language));
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
            return -2;
        }
    }

    protected UrgencyLevel parseUrgency(String s) {
        return switch (s.toUpperCase()) {
            case "HIGH" -> UrgencyLevel.HIGH;
            case "LOW"  -> UrgencyLevel.LOW;
            default     -> UrgencyLevel.MEDIUM;
        };
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Gender getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Faculty getFaculty() { return faculty; }
    public Language getLanguage() { return language; }

    public void setFirstName(String v) { this.firstName = v; }
    public void setLastName(String v) { this.lastName = v; }
    public void setPassword(String v) { this.password = v; }
    public void setEmail(String v) { this.email = v; }
    public void setGender(Gender v) { this.gender = v; }
    public void setFaculty(Faculty v) { this.faculty = v; }
    public void setLanguage(Language v) { this.language = v; Messages.setLanguage(v); }

    public boolean checkPassword(String pwd) { return password.equals(pwd); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() { return Objects.hash(username); }

    @Override
    public String toString() {
        return username + " (" + firstName + " " + lastName + ") [" + getClass().getSimpleName() + "]";
    }
}
