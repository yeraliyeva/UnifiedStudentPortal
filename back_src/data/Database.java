package data;

import communication.*;
import education.Book;
import education.Course;
import education.Specialty;
import enums.Faculty;
import users.Student;
import users.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton data store for the entire university system.
 *
 * All collections are accessed through typed methods.
 * No raw public fields — keeps the rest of the code from reaching in unsafely.
 */
public class Database {

    private static Database instance;

    // ── users ─────────────────────────────────────────────────────
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
    private final List<Organization> organizations = new ArrayList<>();

    private Database() {}

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // ONLY FOR TESTING
    public static void resetInstanceForTesting() {
        instance = null;
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

    /** Returns news sorted: Research/pinned news first, then by date */
    public List<News> getAllNews() {
        return newsFeed.stream().sorted().toList();
    }

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

    // ── organizations ─────────────────────────────────────────────

    public void addOrganization(Organization org) { organizations.add(org); }
    public List<Organization> getOrganizations() { return Collections.unmodifiableList(organizations); }

    public Optional<Organization> findOrganizationByName(String name) {
        return organizations.stream()
                .filter(o -> o.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // ── top cited researcher ──────────────────────────────────────

    /** Returns the username of the researcher with the highest h-index (university-wide). */
    public String getTopCitedResearcher() {
        return users.values().stream()
                .filter(u -> u instanceof interfaces.Researcher)
                .max(Comparator.comparingInt(u -> ((interfaces.Researcher) u).calculateHIndex()))
                .map(User::getUsername)
                .orElse("No researchers found");
    }

    /** Returns top cited researcher filtered by faculty/school. */
    public String getTopCitedResearcherByFaculty(Faculty faculty) {
        return users.values().stream()
                .filter(u -> u instanceof interfaces.Researcher && u.getFaculty() == faculty)
                .max(Comparator.comparingInt(u -> ((interfaces.Researcher) u).calculateHIndex()))
                .map(u -> u.getUsername() + " (h-index: "
                        + ((interfaces.Researcher) u).calculateHIndex() + ")")
                .orElse("No researchers in " + faculty);
    }

    /**
     * Auto-generate news about top cited researcher.
     * Requirement: "don't forget to automatically generate news about top cited Researcher"
     */
    public void generateTopResearcherNews() {
        users.values().stream()
                .filter(u -> u instanceof interfaces.Researcher)
                .max(Comparator.comparingInt(u -> ((interfaces.Researcher) u).calculateHIndex()))
                .ifPresent(top -> {
                    int hIndex = ((interfaces.Researcher) top).calculateHIndex();
                    News news = new News(
                            "Research: Top Cited Researcher — " + top.getFirstName() + " " + top.getLastName(),
                            top.getFirstName() + " " + top.getLastName() + " (" + top.getUsername()
                                    + ") is the top cited researcher with h-index " + hIndex + ".",
                            "system");
                    addNews(news);
                });
    }

    // ── sorted user views ─────────────────────────────────────────

    /** Get all students sorted by GPA descending */
    public List<Student> getStudentsByGPA() {
        return getAllStudents().stream()
                .sorted((a, b) -> Double.compare(b.calculateGPA(), a.calculateGPA()))
                .toList();
    }

    /** Get all students sorted alphabetically */
    public List<Student> getStudentsAlphabetically() {
        return getAllStudents().stream()
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName))
                .toList();
    }

    private List<Student> getAllStudents() {
        return users.values().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .toList();
    }
}
