package users;

import communication.News;
import data.Database;
import enums.*;

import java.time.LocalDate;

/**
 * System administrator.
 *
 * Factory pattern: Admin is the single entry point for creating all user types.
 * This keeps user construction logic in one place and out of Main.
 *
 * Also manages news feed and can view logs (simulated).
 */
public class Admin extends User {

    public Admin(String firstName, String lastName, String username,
                 String password, Gender gender, LocalDate dateOfBirth,
                 String email, Faculty faculty) {
        super(firstName, lastName, username, password, gender, dateOfBirth, email, faculty);
    }

    // ══ Factory Methods ══════════════════════════════════════════

    public Student createStudent(String firstName, String lastName, String uname,
                                 String password, Gender gender, LocalDate dob,
                                 String email, Faculty faculty, DegreeType degree, int year) {
        Student s = new Student(firstName, lastName, uname, password, gender, dob, email, faculty, degree, year);
        Database.getInstance().addUser(s);
        System.out.println("Student created: " + uname);
        return s;
    }

    public GraduateStudent createGraduateStudent(String firstName, String lastName, String uname,
                                                  String password, Gender gender, LocalDate dob,
                                                  String email, Faculty faculty, DegreeType degree, int year) {
        GraduateStudent gs = new GraduateStudent(firstName, lastName, uname, password,
                gender, dob, email, faculty, degree, year);
        Database.getInstance().addUser(gs);
        System.out.println("Graduate student created: " + uname);
        return gs;
    }

    public Teacher createTeacher(String firstName, String lastName, String uname,
                                 String password, Gender gender, LocalDate dob,
                                 String email, Faculty faculty, double salary,
                                 LocalDate hireDate, String insurance,
                                 String degree, boolean isProfessor) {
        Teacher t = new Teacher(firstName, lastName, uname, password, gender, dob,
                email, faculty, salary, hireDate, insurance, degree, isProfessor);
        Database.getInstance().addUser(t);
        System.out.println("Teacher created: " + uname);
        return t;
    }

    public Dean createDean(String firstName, String lastName, String uname,
                           String password, Gender gender, LocalDate dob,
                           String email, Faculty faculty, double salary,
                           LocalDate hireDate, String insurance, String degree) {
        Dean d = new Dean(firstName, lastName, uname, password, gender, dob,
                email, faculty, salary, hireDate, insurance, degree);
        Database.getInstance().addUser(d);
        System.out.println("Dean created: " + uname);
        return d;
    }

    public Manager createManager(String firstName, String lastName, String uname,
                                 String password, Gender gender, LocalDate dob,
                                 String email, Faculty faculty, double salary,
                                 LocalDate hireDate, String insurance,
                                 ManagerPosition position) {
        Manager m = new Manager(firstName, lastName, uname, password, gender, dob,
                email, faculty, salary, hireDate, insurance, position);
        Database.getInstance().addUser(m);
        System.out.println("Manager created: " + uname);
        return m;
    }

    public Librarian createLibrarian(String firstName, String lastName, String uname,
                                     String password, Gender gender, LocalDate dob,
                                     String email, Faculty faculty, double salary,
                                     LocalDate hireDate, String insurance) {
        Librarian l = new Librarian(firstName, lastName, uname, password, gender, dob,
                email, faculty, salary, hireDate, insurance);
        Database.getInstance().addUser(l);
        System.out.println("Librarian created: " + uname);
        return l;
    }

    public TechSupport createTechSupport(String firstName, String lastName, String uname,
                                         String password, Gender gender, LocalDate dob,
                                         String email, Faculty faculty, double salary,
                                         LocalDate hireDate, String insurance) {
        TechSupport ts = new TechSupport(firstName, lastName, uname, password, gender, dob,
                email, faculty, salary, hireDate, insurance);
        Database.getInstance().addUser(ts);
        System.out.println("TechSupport created: " + uname);
        return ts;
    }

    public EmployeeResearcher createEmployeeResearcher(String firstName, String lastName, String uname,
                                                        String password, Gender gender, LocalDate dob,
                                                        String email, Faculty faculty, double salary,
                                                        LocalDate hireDate, String insurance,
                                                        String researchField) {
        EmployeeResearcher er = new EmployeeResearcher(firstName, lastName, uname, password, gender, dob,
                email, faculty, salary, hireDate, insurance, researchField);
        Database.getInstance().addUser(er);
        System.out.println("EmployeeResearcher created: " + uname);
        return er;
    }

    // ══ User management ══════════════════════════════════════════

    public void removeUser(String username) {
        if (username.equals(getUsername())) {
            System.out.println("Cannot remove yourself.");
            return;
        }
        if (Database.getInstance().userExists(username)) {
            Database.getInstance().removeUser(username);
            System.out.println("User removed: " + username);
        } else {
            System.out.println("User not found: " + username);
        }
    }

    public void listAllUsers() {
        System.out.println("\n=== ALL USERS ===");
        Database.getInstance().getAllUsers().forEach(System.out::println);
    }

    public void editUserInteractive() {
        System.out.print("Username to edit: ");
        String uname = scanner.nextLine().trim();
        User user = Database.getInstance().getUser(uname);
        if (user == null) { System.out.println("User not found."); return; }
        System.out.println("1. Change email  2. Change password  3. Change faculty");
        switch (readInt()) {
            case 1 -> {
                System.out.print("New email: "); user.setEmail(scanner.nextLine().trim());
                System.out.println("Email updated.");
            }
            case 2 -> {
                System.out.print("New password: "); user.setPassword(scanner.nextLine().trim());
                System.out.println("Password updated.");
            }
            case 3 -> {
                System.out.print("New faculty (" + java.util.Arrays.toString(Faculty.values()) + "): ");
                try { user.setFaculty(Faculty.valueOf(scanner.nextLine().trim().toUpperCase())); System.out.println("Faculty updated."); }
                catch (Exception e) { System.out.println("Invalid faculty."); }
            }
        }
    }

    // ══ News management ═══════════════════════════════════════════

    public void addNewsInteractive() {
        System.out.print("News title: ");  String title = scanner.nextLine().trim();
        System.out.print("News body: ");   String body  = scanner.nextLine().trim();
        News news = new News(title, body, getUsername());
        Database.getInstance().addNews(news);
        System.out.println("News added: " + news);
    }

    public void editNewsInteractive() {
        viewNews();
        System.out.print("News ID to edit: ");
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            System.out.print("New title: ");  String title = scanner.nextLine().trim();
            System.out.print("New body: ");   String body  = scanner.nextLine().trim();
            news.edit(title, body);
            System.out.println("News updated.");
        }, () -> System.out.println("News not found."));
    }

    public void deleteNewsInteractive() {
        viewNews();
        System.out.print("News ID to delete: ");
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            Database.getInstance().removeNews(news);
            System.out.println("News deleted.");
        }, () -> System.out.println("News not found."));
    }

    // ══ System ═══════════════════════════════════════════════════

    public void viewLogs() {
        System.out.println("[LOG] System log viewer — not yet implemented (feature not working per spec).");
    }

    public void viewTopCitedResearcher() {
        System.out.println("Top cited researcher: " + Database.getInstance().getTopCitedResearcher());
    }

    // ══ Menu ══════════════════════════════════════════════════════

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== ADMIN MENU ===
                    1. Create user
                    2. Remove user
                    3. List all users
                    4. Edit user
                    5. Add news
                    6. Edit news
                    7. Delete news
                    8. View news
                    9. View inbox
                    10. Send message
                    11. View top cited researcher
                    12. View logs
                    13. Personal info
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1  -> createUserInteractive();
                case 2  -> { System.out.print("Username: "); removeUser(scanner.nextLine().trim()); }
                case 3  -> listAllUsers();
                case 4  -> editUserInteractive();
                case 5  -> addNewsInteractive();
                case 6  -> editNewsInteractive();
                case 7  -> deleteNewsInteractive();
                case 8  -> viewNews();
                case 9  -> viewInbox();
                case 10 -> sendMessageInteractive();
                case 11 -> viewTopCitedResearcher();
                case 12 -> viewLogs();
                case 13 -> viewPersonalInfo();
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void createUserInteractive() {
        System.out.println("""
                Create:
                1. Student          2. Graduate Student
                3. Teacher          4. Dean
                5. Manager          6. Librarian
                7. Tech Support     8. Employee Researcher""");
        System.out.print("> ");
        int choice = readInt();

        // Collect common fields
        System.out.print("First name: ");    String fn    = scanner.nextLine().trim();
        System.out.print("Last name: ");     String ln    = scanner.nextLine().trim();
        System.out.print("Username: ");      String uname = scanner.nextLine().trim();

        if (Database.getInstance().userExists(uname)) {
            System.out.println("Username already taken."); return;
        }
        System.out.print("Password: ");      String pwd   = scanner.nextLine().trim();
        System.out.print("Email: ");         String email = scanner.nextLine().trim();
        System.out.print("Faculty (" + java.util.Arrays.toString(Faculty.values()) + "): ");
        Faculty faculty;
        try { faculty = Faculty.valueOf(scanner.nextLine().trim().toUpperCase()); }
        catch (Exception e) { faculty = Faculty.SITE; }

        LocalDate dob = LocalDate.of(2000, 1, 1); // default
        Gender gender = Gender.UNDEFINED;

        switch (choice) {
            case 1 -> createStudent(fn, ln, uname, pwd, gender, dob, email, faculty, DegreeType.BACHELOR, 1);
            case 2 -> createGraduateStudent(fn, ln, uname, pwd, gender, dob, email, faculty, DegreeType.MASTER, 1);
            case 3 -> createTeacher(fn, ln, uname, pwd, gender, dob, email, faculty, 100000, LocalDate.now(), "INS-001", "PhD", false);
            case 4 -> createDean(fn, ln, uname, pwd, gender, dob, email, faculty, 150000, LocalDate.now(), "INS-002", "PhD");
            case 5 -> {
                System.out.println("Position (OR / DEANS_OFFICE / DEPARTMENT): ");
                ManagerPosition pos;
                try { pos = ManagerPosition.valueOf(scanner.nextLine().trim().toUpperCase()); }
                catch (Exception e) { pos = ManagerPosition.OR; }
                createManager(fn, ln, uname, pwd, gender, dob, email, faculty, 90000, LocalDate.now(), "INS-003", pos);
            }
            case 6 -> createLibrarian(fn, ln, uname, pwd, gender, dob, email, faculty, 80000, LocalDate.now(), "INS-004");
            case 7 -> createTechSupport(fn, ln, uname, pwd, gender, dob, email, faculty, 75000, LocalDate.now(), "INS-005");
            case 8 -> {
                System.out.print("Research field: ");
                String field = scanner.nextLine().trim();
                createEmployeeResearcher(fn, ln, uname, pwd, gender, dob, email, faculty, 95000, LocalDate.now(), "INS-006", field);
            }
            default -> System.out.println("Invalid type.");
        }
    }
}
