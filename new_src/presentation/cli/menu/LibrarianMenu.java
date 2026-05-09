package presentation.cli.menu;

import application.Result;
import application.usecase.library.AddBook;
import application.usecase.library.RemoveBook;
import domain.repository.BookRepository;
import domain.user.Librarian;
import presentation.cli.Console;

import java.util.List;

public final class LibrarianMenu extends Menu {
    private final Librarian librarian;
    private final BookRepository books;
    private final AddBook addBook;
    private final RemoveBook removeBook;

    public LibrarianMenu(Console console, Librarian librarian, BookRepository books,
                         AddBook addBook, RemoveBook removeBook) {
        super(console);
        this.librarian = librarian;
        this.books = books;
        this.addBook = addBook;
        this.removeBook = removeBook;
    }

    @Override protected String title() { return "=== LIBRARIAN MENU (" + librarian.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        return List.of(
                new MenuItem("View all books", this::viewBooks),
                new MenuItem("Add book", this::addInteractive),
                new MenuItem("Remove book", this::removeInteractive)
        );
    }

    private void viewBooks() {
        if (books.findAll().isEmpty()) { console.println("No books."); return; }
        books.findAll().forEach(b -> console.println("  " + b));
    }

    private void addInteractive() {
        String title = console.readLine("Title:");
        String author = console.readLine("Author:");
        Result r = addBook.execute(librarian.username(), title, author);
        console.println(r.message());
    }

    private void removeInteractive() {
        String title = console.readLine("Title:");
        Result r = removeBook.execute(librarian.username(), title);
        console.println(r.message());
    }
}
