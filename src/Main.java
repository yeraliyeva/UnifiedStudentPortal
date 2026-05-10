import bootstrap.AppContext;
import bootstrap.DataSeeder;
import bootstrap.IdSequenceWarmup;
import domain.user.User;
import presentation.cli.menu.Menu;
import presentation.rest.server.RestServer;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Application entry point.
 *
 * <p>Two launch modes:
 * <ul>
 *   <li>{@code java Main}                — starts the interactive CLI (default)</li>
 *   <li>{@code java Main --server [port]} — starts the REST API (default port: 8080)</li>
 * </ul>
 *
 * Both modes share the exact same {@link AppContext}: same repositories, same use cases,
 * same data directory — only the Presentation Layer differs.
 */
public final class Main {
    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of(System.getProperty("uni.data", "data"));
        AppContext ctx = AppContext.withJsonStorage(dataDir);
        new DataSeeder(ctx).seedIfEmpty();
        IdSequenceWarmup.warm(ctx);

        if (args.length > 0 && args[0].equals("--server")) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
            RestServer server = new RestServer(ctx, port);
            server.start();
            System.out.println("=== UNIVERSITY REST API ===");
            System.out.println("Listening on http://localhost:" + port + "/api");
            System.out.println("Press Ctrl+C to stop.\n");
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            Thread.currentThread().join(); // keep main thread alive
        } else {
            runCli(ctx);
        }
    }

    // ── CLI mode (unchanged) ──────────────────────────────────

    private static void runCli(AppContext ctx) {
        ctx.console.println("\n=== UNIVERSITY MANAGEMENT SYSTEM ===");
        while (true) {
            Optional<User> user = ctx.loginScreen.run();
            if (user.isEmpty()) {
                String again = ctx.console.readLine("Try again? (y/n):");
                if (!again.equalsIgnoreCase("y")) break;
                continue;
            }
            ctx.console.println("\nWelcome, " + user.get().name().full()
                    + " [" + user.get().getClass().getSimpleName() + "]");
            Menu menu = ctx.menuFactory.menuFor(user.get());
            menu.run();
            ctx.console.println("Logged out.\n");
        }
        ctx.console.println("Goodbye.");
    }
}
