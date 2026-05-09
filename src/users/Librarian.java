package users;

import data.Database;
import education.Book;
import enums.Faculty;
import enums.Gender;
import interfaces.Subscriber;

import java.time.LocalDate;
import java.util.List;

/**
 * Librarian manages the library: adds/removes books, notifies users.
 */
public class Librarian extends Employee implements Subscriber {

    private final List<String> notifications = new java.util.ArrayList<>();

    public Librarian(String firstName, String lastName, String username,
                     String password, Gender gender, LocalDate dateOfBirth,
                     String email, Faculty faculty, double salary,
                     LocalDate hireDate, String insuranceNumber) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
    }

    public void addBook(String title, String author) {
        Book book = new Book(title, author);
        Database.getInstance().addBook(book);
        System.out.println("Book added: " + book);
    }

    public void removeBook(String title) {
        Database.getInstance().getBooks().stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(book -> {
                    if (book.isBorrowed()) {
                        System.out.println("Cannot remove a borrowed book.");
                    } else {
                        Database.getInstance().removeBook(book);
                        System.out.println("Book removed: " + title);
                    }
                }, () -> System.out.println("Book not found."));
    }

    public void viewAllBooks() {
        System.out.println("\n=== LIBRARY CATALOGUE ===");
        var books = Database.getInstance().getBooks();
        if (books.isEmpty()) { System.out.println("No books."); return; }
        books.forEach(System.out::println);
    }

    @Override
    public void notifyNewPaper(String journalName, communication.ResearchPaper paper) {
        String note = "[LIBRARY] New paper in " + journalName + ": " + paper.getTitle();
        notifications.add(note);
        System.out.println(note);
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== LIBRARIAN MENU ===
                    1. View all books
                    2. Add book
                    3. Remove book
                    4. View inbox
                    5. Send message
                    6. View news
                    7. Personal info
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1 -> viewAllBooks();
                case 2 -> {
                    System.out.print("Title: "); String title = scanner.nextLine().trim();
                    System.out.print("Author: "); String author = scanner.nextLine().trim();
                    addBook(title, author);
                }
                case 3 -> {
                    System.out.print("Title to remove: ");
                    removeBook(scanner.nextLine().trim());
                }
                case 4 -> viewInbox();
                case 5 -> sendMessageInteractive();
                case 6 -> viewNews();
                case 7 -> viewPersonalInfo();
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
