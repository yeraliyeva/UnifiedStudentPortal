package users;

import common.LogManager;
import common.Messages;
import communication.ResearchPaper;
import data.Database;
import education.Book;
import enums.Faculty;
import enums.Gender;
import interfaces.Subscriber;

import java.time.LocalDate;

public class Librarian extends Employee implements Subscriber {

    private final java.util.Set<String> subscribedJournals = new java.util.LinkedHashSet<>();

    public Librarian(String firstName, String lastName, String username,
                     String password, Gender gender, LocalDate dateOfBirth,
                     String email, Faculty faculty, double salary,
                     LocalDate hireDate, String insuranceNumber) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
    }

    public void viewAllBooks() {
        System.out.println(Messages.get("librarian.books.title"));
        var books = Database.getInstance().getBooks();
        if (books.isEmpty()) { System.out.println(Messages.get("librarian.books.empty")); return; }
        books.forEach(b -> System.out.println(b + " | " + (b.isBorrowed() ? "Borrowed" : "Available")));
    }

    public void addBookInteractive() {
        System.out.print(Messages.get("librarian.books.add_title"));
        String title = scanner.nextLine().trim();
        System.out.print(Messages.get("librarian.books.add_author"));
        String author = scanner.nextLine().trim();
        Book book = new Book(title, author);
        Database.getInstance().addBook(book);
        LogManager.getInstance().log(getUsername(), "Added book: " + title);
        System.out.println(Messages.fmt("librarian.books.added", title));
    }

    public void removeBookInteractive() {
        viewAllBooks();
        System.out.print(Messages.get("librarian.books.remove_prompt"));
        String title = scanner.nextLine().trim();
        Database.getInstance().findBookByTitle(title).ifPresentOrElse(b -> {
            if (b.isBorrowed()) {
                System.out.println(Messages.get("librarian.books.borrowed_cant"));
            } else {
                Database.getInstance().removeBook(b);
                LogManager.getInstance().log(getUsername(), "Removed book: " + title);
                System.out.println(Messages.fmt("librarian.books.removed", title));
            }
        }, () -> System.out.println(Messages.get("librarian.books.not_found")));
    }

    @Override
    public void notifyNewPaper(String journalName, ResearchPaper paper) {
        System.out.println("[LIBRARY NOTIFICATION] New paper in '" + journalName + "': " + paper.getTitle());
    }

    public void subscribeJournalInteractive() {
        Database.getInstance().getResearchProjects().forEach(System.out::println);
        System.out.print(Messages.get("subscription.journal_prompt"));
        String journal = scanner.nextLine().trim();
        Database.getInstance().findProjectByJournal(journal).ifPresentOrElse(proj -> {
            proj.subscribe(this);
            subscribedJournals.add(journal);
            LogManager.getInstance().log(getUsername(), "Subscribed to journal: " + journal);
            System.out.println(Messages.fmt("subscription.subscribed", journal));
        }, () -> System.out.println(Messages.fmt("subscription.journal_not_found", journal)));
    }

    public void unsubscribeJournalInteractive() {
        viewSubscriptions();
        System.out.print(Messages.get("subscription.unsub_prompt"));
        String journal = scanner.nextLine().trim();
        Database.getInstance().findProjectByJournal(journal).ifPresent(proj -> {
            proj.unsubscribe(this);
            subscribedJournals.remove(journal);
            LogManager.getInstance().log(getUsername(), "Unsubscribed from journal: " + journal);
            System.out.println(Messages.fmt("subscription.unsubscribed", journal));
        });
    }

    public void viewSubscriptions() {
        System.out.println(Messages.get("subscription.title"));
        if (subscribedJournals.isEmpty()) { System.out.println(Messages.get("subscription.empty")); return; }
        subscribedJournals.forEach(j -> System.out.println("  - " + j));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("librarian.menu.title"));
            System.out.println("1. " + Messages.get("librarian.menu.1"));
            System.out.println("2. " + Messages.get("librarian.menu.2"));
            System.out.println("3. " + Messages.get("librarian.menu.3"));
            System.out.println("4. " + Messages.get("librarian.menu.4"));
            System.out.println("5. " + Messages.get("librarian.menu.5"));
            System.out.println("6. " + Messages.get("librarian.menu.6"));
            System.out.println("7. " + Messages.get("librarian.menu.7"));
            System.out.println("8. " + Messages.get("librarian.menu.8"));
            System.out.println("9. " + Messages.get("librarian.menu.9"));
            System.out.println("10. " + Messages.get("librarian.menu.10"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            switch (readInt()) {
                case 1  -> viewAllBooks();
                case 2  -> addBookInteractive();
                case 3  -> removeBookInteractive();
                case 4  -> viewInbox();
                case 5  -> sendMessageInteractive();
                case 6  -> viewNews();
                case 7  -> { viewPersonalInfo(); editPersonalInfo(); }
                case 8  -> subscribeJournalInteractive();
                case 9  -> unsubscribeJournalInteractive();
                case 10 -> viewSubscriptions();
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }
}
