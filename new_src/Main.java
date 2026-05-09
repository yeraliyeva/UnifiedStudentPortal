import bootstrap.AppContext;
import bootstrap.DataSeeder;
import bootstrap.IdSequenceWarmup;
import domain.user.User;
import presentation.cli.menu.Menu;

import java.nio.file.Path;
import java.util.Optional;

public final class Main {
    public static void main(String[] args) {
        Path dataDir = Path.of(System.getProperty("uni.data", "data"));
        AppContext ctx = AppContext.withJsonStorage(dataDir);
        new DataSeeder(ctx).seedIfEmpty();
        IdSequenceWarmup.warm(ctx);

        ctx.console.println("\n=== UNIVERSITY MANAGEMENT SYSTEM ===");
        while (true) {
            Optional<User> user = ctx.loginScreen.run();
            if (user.isEmpty()) {
                String again = ctx.console.readLine("Try again? (y/n):");
                if (!again.equalsIgnoreCase("y")) break;
                continue;
            }
            ctx.console.println("\nWelcome, " + user.get().name().full() + " [" + user.get().getClass().getSimpleName() + "]");
            Menu menu = ctx.menuFactory.menuFor(user.get());
            menu.run();
            ctx.console.println("Logged out.\n");
        }
        ctx.console.println("Goodbye.");
    }
}
