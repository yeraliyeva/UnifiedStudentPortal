package data;

import communication.Message;
import communication.News;
import communication.Order;
import communication.Request;
import communication.ResearchProject;
import education.Book;
import education.Course;
import education.Specialty;
import users.User;

import java.util.*;

/**
 * Singleton data store for the entire university system.
 *
 * Design note: All collections are accessed through typed methods.
 * No raw public fields — keeps the rest of the code from reaching in unsafely.
 *
 * Removed: serialization (was causing issues per team report), logging (not implemented).
 */
public class Database {

    private static Database instance;

    // ── users ─────────────────────────────────────────────────────
    // key = username
    private final Map<String, User> users = new LinkedHashMap<>();

    // ── education ─────────────────────────────────────────────────
    private final List<Course> courses = new ArrayList<>();
    private final List<Specialty> specialties = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();

    // ── communication ─────────────────────────────────────────────
    private final List<Message> messages = new ArrayList<>();
    private final List<News> newsFeed = new ArrayList<>();
    private final List<Request> requests = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<ResearchProject> researchProjects = new ArrayList<>();

    private Database() {}

    public static Database getInstance() {
        if (instance == null) instance = new Database();
        return instance;
    }

    // ── users ─────────────────────────────────────────────────────

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    // ── courses ───────────────────────────────────────────────────

    public void addCourse(Course course) { courses.add(course); }
    public void removeCourse(Course course) { courses.remove(course); }
    public List<Course> getCourses() { return Collections.unmodifiableList(courses); }

    public Optional<Course> findCourseById(String id) {
        return courses.stream().filter(c -> c.getCourseId().equals(id)).findFirst();
    }

    // ── specialties ───────────────────────────────────────────────

    public void addSpecialty(Specialty s) { specialties.add(s); }
    public List<Specialty> getSpecialties() { return Collections.unmodifiableList(specialties); }

    // ── books ─────────────────────────────────────────────────────

    public void addBook(Book book) { books.add(book); }
    public void removeBook(Book book) { books.remove(book); }
    public List<Book> getBooks() { return Collections.unmodifiableList(books); }

    public Optional<Book> findBookByTitle(String title) {
        return books.stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title) && !b.isBorrowed())
                .findFirst();
    }

    // ── messages ──────────────────────────────────────────────────

    public void addMessage(Message msg) { messages.add(msg); }

    public List<Message> getMessagesFor(String username) {
        return messages.stream()
                .filter(m -> m.getRecipient().equals(username))
                .sorted()
                .toList();
    }

    // ── news ──────────────────────────────────────────────────────

    public void addNews(News news) { newsFeed.add(news); }
    public void removeNews(News news) { newsFeed.remove(news); }
    public List<News> getAllNews() { return Collections.unmodifiableList(newsFeed); }

    public Optional<News> findNewsById(int id) {
        return newsFeed.stream().filter(n -> n.getId() == id).findFirst();
    }

    // ── requests ──────────────────────────────────────────────────

    public void addRequest(Request request) { requests.add(request); }
    public List<Request> getAllRequests() { return Collections.unmodifiableList(requests); }

    public Optional<Request> findRequestById(int id) {
        return requests.stream().filter(r -> r.getId() == id).findFirst();
    }

    // ── orders ────────────────────────────────────────────────────

    public void addOrder(Order order) { orders.add(order); }
    public List<Order> getAllOrders() { return Collections.unmodifiableList(orders); }

    public Optional<Order> findOrderById(int id) {
        return orders.stream().filter(o -> o.getId() == id).findFirst();
    }

    // ── research projects ─────────────────────────────────────────

    public void addResearchProject(ResearchProject project) { researchProjects.add(project); }
    public List<ResearchProject> getResearchProjects() { return Collections.unmodifiableList(researchProjects); }

    public Optional<ResearchProject> findProjectByJournal(String journalName) {
        return researchProjects.stream()
                .filter(p -> p.getJournalName().equalsIgnoreCase(journalName))
                .findFirst();
    }

    // ── top cited researcher ──────────────────────────────────────

    /**
     * Returns the username of the researcher with the highest h-index.
     * Requirement: system supports printing top cited researcher.
     */
    public String getTopCitedResearcher() {
        return users.values().stream()
                .filter(u -> u instanceof interfaces.Researcher)
                .max(Comparator.comparingInt(u -> ((interfaces.Researcher) u).calculateHIndex()))
                .map(User::getUsername)
                .orElse("No researchers found");
    }
}
