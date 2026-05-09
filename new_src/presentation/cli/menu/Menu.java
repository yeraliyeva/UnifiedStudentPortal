package presentation.cli.menu;

import presentation.cli.Console;

import java.util.List;

public abstract class Menu {
    protected final Console console;

    protected Menu(Console console) { this.console = console; }

    protected abstract String title();
    protected abstract List<MenuItem> items();

    public void run() {
        while (true) {
            List<MenuItem> items = items();
            console.println();
            console.println(title());
            for (int i = 0; i < items.size(); i++) {
                console.println((i + 1) + ". " + items.get(i).label());
            }
            console.println("0. Log out");
            int choice = console.readInt(">");
            if (choice == 0) return;
            if (choice >= 1 && choice <= items.size()) {
                try { items.get(choice - 1).action().run(); }
                catch (RuntimeException e) { console.println("Error: " + e.getMessage()); }
            } else {
                console.println("Invalid option.");
            }
        }
    }
}
