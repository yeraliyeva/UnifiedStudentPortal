package presentation.rest.server;

import bootstrap.AppContext;
import com.sun.net.httpserver.HttpServer;
import domain.user.*;
import presentation.rest.auth.SecurityFilter;
import presentation.rest.auth.TokenStore;
import presentation.rest.controller.*;
import presentation.rest.routing.HttpMethod;
import presentation.rest.routing.Route;
import presentation.rest.routing.Router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/** Wires together all controllers, registers routes, and manages the HTTP server lifecycle. */
public final class RestServer {
    private final HttpServer server;

    public RestServer(AppContext ctx, int port) throws IOException {
        TokenStore     tokens   = new TokenStore();
        SecurityFilter security = new SecurityFilter(tokens, ctx.userRepository);
        Router         router   = new Router(security);

        AuthController      auth      = new AuthController(ctx, tokens);
        AdminController     admin     = new AdminController(ctx);
        CourseController    courses   = new CourseController(ctx);
        LibraryController   library   = new LibraryController(ctx);
        MessagingController messaging = new MessagingController(ctx);
        ResearchController  research  = new ResearchController(ctx);

        router.register(Route.of(HttpMethod.POST, "/api/login",  auth::login));
        router.register(Route.of(HttpMethod.POST, "/api/logout", auth::logout));

        router.register(Route.of(HttpMethod.GET,    "/api/users",            admin::listUsers,    Admin.class));
        router.register(Route.of(HttpMethod.GET,    "/api/users/{username}", admin::getUser,      Admin.class));
        router.register(Route.of(HttpMethod.POST,   "/api/users/students",   admin::createStudent,Admin.class));
        router.register(Route.of(HttpMethod.DELETE, "/api/users/{username}", admin::deleteUser,   Admin.class));
        router.register(Route.of(HttpMethod.GET,    "/api/logs",             admin::viewLogs,     Admin.class));
        router.register(Route.of(HttpMethod.GET,    "/api/reports/academic", admin::academicReport,Admin.class));

        router.register(Route.of(HttpMethod.GET,  "/api/courses",               courses::listCourses, User.class));
        router.register(Route.of(HttpMethod.GET,  "/api/courses/{id}",          courses::getCourse,   User.class));
        router.register(Route.of(HttpMethod.POST, "/api/courses",               courses::createCourse,Manager.class));
        router.register(Route.of(HttpMethod.POST, "/api/courses/{id}/enroll",   courses::enroll,      Student.class));
        router.register(Route.of(HttpMethod.POST, "/api/courses/{id}/drop",     courses::drop,        Student.class));
        router.register(Route.of(HttpMethod.POST, "/api/courses/{id}/marks",    courses::recordMarks, Teacher.class));
        router.register(Route.of(HttpMethod.GET,  "/api/courses/{id}/grades",   courses::viewGrades,  Teacher.class));
        router.register(Route.of(HttpMethod.GET,  "/api/transcript",            courses::transcript,  Student.class));

        router.register(Route.of(HttpMethod.GET,    "/api/books",                library::listBooks,  User.class));
        router.register(Route.of(HttpMethod.POST,   "/api/books",                library::addBook,    Librarian.class));
        router.register(Route.of(HttpMethod.DELETE, "/api/books/{title}",        library::removeBook, Librarian.class));
        router.register(Route.of(HttpMethod.POST,   "/api/books/{title}/borrow", library::borrowBook, User.class));
        router.register(Route.of(HttpMethod.POST,   "/api/books/{title}/return", library::returnBook, User.class));

        router.register(Route.of(HttpMethod.GET,  "/api/messages/inbox",       messaging::inbox,        User.class));
        router.register(Route.of(HttpMethod.POST, "/api/messages",             messaging::sendMessage,  User.class));
        router.register(Route.of(HttpMethod.GET,  "/api/news",                 messaging::listNews,     User.class));
        router.register(Route.of(HttpMethod.POST, "/api/news",                 messaging::publishNews,  Employee.class));
        router.register(Route.of(HttpMethod.POST, "/api/news/{id}/comment",    messaging::commentOnNews,User.class));
        router.register(Route.of(HttpMethod.GET,  "/api/requests",             messaging::listRequests, User.class));
        router.register(Route.of(HttpMethod.POST, "/api/requests",             messaging::submitRequest,User.class));
        router.register(Route.of(HttpMethod.GET,  "/api/orders",               messaging::listOrders,   TechSupport.class));
        router.register(Route.of(HttpMethod.POST, "/api/orders",               messaging::createOrder,  User.class));
        router.register(Route.of(HttpMethod.PUT,  "/api/orders/{id}/accept",   messaging::acceptOrder,  TechSupport.class));
        router.register(Route.of(HttpMethod.PUT,  "/api/orders/{id}/complete", messaging::completeOrder,TechSupport.class));

        router.register(Route.of(HttpMethod.GET,    "/api/papers",                  research::listPapers,   User.class));
        router.register(Route.of(HttpMethod.POST,   "/api/papers",                  research::publishPaper, User.class));
        router.register(Route.of(HttpMethod.GET,    "/api/papers/{id}/cite",        research::getCitation,  User.class));
        router.register(Route.of(HttpMethod.GET,    "/api/projects",                research::listProjects, User.class));
        router.register(Route.of(HttpMethod.POST,   "/api/projects",                research::createProject,User.class));
        router.register(Route.of(HttpMethod.POST,   "/api/projects/{journal}/join", research::joinProject,  User.class));
        router.register(Route.of(HttpMethod.POST,   "/api/subscriptions",           research::subscribe,    User.class));
        router.register(Route.of(HttpMethod.DELETE, "/api/subscriptions/{journal}", research::unsubscribe,  User.class));

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", router);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void start() { server.start(); }
    public void stop()  { server.stop(1); }
}

