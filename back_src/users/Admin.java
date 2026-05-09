package users;

import common.LogManager;
import common.Messages;
import communication.News;
import data.Database;
import enums.*;

import java.time.LocalDate;

public class Admin extends User {

    public Admin(String firstName, String lastName, String username,
                 String password, Gender gender, LocalDate dateOfBirth,
                 String email, Faculty faculty) {
        super(firstName, lastName, username, password, gender, dateOfBirth, email, faculty);
    }

    public Student createStudent(String firstName, String lastName, String username,
                                 String password, Gender gender, LocalDate dob,
                                 String email, Faculty faculty, DegreeType degree, int year) {
        Student s = new Student(firstName, lastName, username, password, gender, dob, email, faculty, degree, year);
        Database.getInstance().addUser(s);
        LogManager.getInstance().log(getUsername(), "Created student: " + username);
        return s;
    }

    public GraduateStudent createGraduateStudent(String firstName, String lastName, String username,
                                                  String password, Gender gender, LocalDate dob,
                                                  String email, Faculty faculty, DegreeType degree, int year) {
        GraduateStudent gs = new GraduateStudent(firstName, lastName, username, password, gender, dob, email, faculty, degree, year);
        Database.getInstance().addUser(gs);
        LogManager.getInstance().log(getUsername(), "Created grad student: " + username);
        return gs;
    }

    public Teacher createTeacher(String firstName, String lastName, String username,
                                  String password, Gender gender, LocalDate dob,
                                  String email, Faculty faculty, double salary,
                                  LocalDate hireDate, String insNum, String degree, boolean isProfessor) {
        Teacher t = new Teacher(firstName, lastName, username, password, gender, dob, email,
                faculty, salary, hireDate, insNum, degree, isProfessor);
        Database.getInstance().addUser(t);
        LogManager.getInstance().log(getUsername(), "Created teacher: " + username);
        return t;
    }

    public Dean createDean(String firstName, String lastName, String username,
                            String password, Gender gender, LocalDate dob,
                            String email, Faculty faculty, double salary,
                            LocalDate hireDate, String insNum, String degree) {
        Dean d = new Dean(firstName, lastName, username, password, gender, dob, email,
                faculty, salary, hireDate, insNum, degree);
        Database.getInstance().addUser(d);
        LogManager.getInstance().log(getUsername(), "Created dean: " + username);
        return d;
    }

    public Manager createManager(String firstName, String lastName, String username,
                                  String password, Gender gender, LocalDate dob,
                                  String email, Faculty faculty, double salary,
                                  LocalDate hireDate, String insNum, ManagerPosition pos) {
        Manager m = new Manager(firstName, lastName, username, password, gender, dob, email,
                faculty, salary, hireDate, insNum, pos);
        Database.getInstance().addUser(m);
        LogManager.getInstance().log(getUsername(), "Created manager: " + username);
        return m;
    }

    public Librarian createLibrarian(String firstName, String lastName, String username,
                                      String password, Gender gender, LocalDate dob,
                                      String email, Faculty faculty, double salary,
                                      LocalDate hireDate, String insNum) {
        Librarian l = new Librarian(firstName, lastName, username, password, gender, dob, email,
                faculty, salary, hireDate, insNum);
        Database.getInstance().addUser(l);
        LogManager.getInstance().log(getUsername(), "Created librarian: " + username);
        return l;
    }

    public TechSupport createTechSupport(String firstName, String lastName, String username,
                                          String password, Gender gender, LocalDate dob,
                                          String email, Faculty faculty, double salary,
                                          LocalDate hireDate, String insNum) {
        TechSupport ts = new TechSupport(firstName, lastName, username, password, gender, dob, email,
                faculty, salary, hireDate, insNum);
        Database.getInstance().addUser(ts);
        LogManager.getInstance().log(getUsername(), "Created tech support: " + username);
        return ts;
    }

    public EmployeeResearcher createEmployeeResearcher(String firstName, String lastName, String username,
                                                        String password, Gender gender, LocalDate dob,
                                                        String email, Faculty faculty, double salary,
                                                        LocalDate hireDate, String insNum, String field) {
        EmployeeResearcher er = new EmployeeResearcher(firstName, lastName, username, password, gender,
                dob, email, faculty, salary, hireDate, insNum, field);
        Database.getInstance().addUser(er);
        LogManager.getInstance().log(getUsername(), "Created employee researcher: " + username);
        return er;
    }

    public void viewAllUsers() {
        System.out.println(Messages.get("admin.users.title"));
        Database.getInstance().getAllUsers().forEach(System.out::println);
    }

    public void removeUser(String username) {
        if (Database.getInstance().userExists(username)) {
            Database.getInstance().removeUser(username);
            LogManager.getInstance().log(getUsername(), "Removed user: " + username);
            System.out.println(Messages.fmt("admin.users.removed", username));
        } else {
            System.out.println(Messages.get("admin.users.not_found"));
        }
    }

    public void addNewsInteractive() {
        System.out.print(Messages.get("admin.news.title_prompt"));
        String title = scanner.nextLine().trim();
        System.out.print(Messages.get("admin.news.body_prompt"));
        String body = scanner.nextLine().trim();
        News news = new News(title, body, getUsername());
        Database.getInstance().addNews(news);
        LogManager.getInstance().log(getUsername(), "Published news: " + title);
        System.out.println(Messages.fmt("admin.news.published", news));
    }

    public void editNewsInteractive() {
        viewNews();
        System.out.print(Messages.get("admin.news.edit_id"));
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            System.out.print(Messages.get("admin.news.new_title"));
            String title = scanner.nextLine().trim();
            System.out.print(Messages.get("admin.news.new_body"));
            String body = scanner.nextLine().trim();
            news.edit(title, body);
            LogManager.getInstance().log(getUsername(), "Edited news #" + id);
            System.out.println(Messages.get("admin.news.updated"));
        }, () -> System.out.println(Messages.get("common.not_found")));
    }

    public void deleteNewsInteractive() {
        viewNews();
        System.out.print(Messages.get("admin.news.delete_id"));
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            Database.getInstance().removeNews(news);
            LogManager.getInstance().log(getUsername(), "Deleted news #" + id);
            System.out.println(Messages.get("admin.news.deleted"));
        }, () -> System.out.println(Messages.get("common.not_found")));
    }

    public void viewLogs() {
        System.out.println(Messages.get("admin.logs.title"));
        var logs = LogManager.getInstance().getLogs();
        if (logs.isEmpty()) {
            System.out.println(Messages.get("admin.logs.empty"));
        } else {
            logs.forEach(System.out::println);
        }
    }

    public void viewLogsForUser() {
        System.out.print(Messages.get("admin.logs.user_prompt"));
        String username = scanner.nextLine().trim();
        System.out.println(Messages.fmt("admin.logs.user_title", username));
        var logs = LogManager.getInstance().getLogsForUser(username);
        if (logs.isEmpty()) {
            System.out.println(Messages.fmt("admin.logs.user_empty", username));
        } else {
            logs.forEach(System.out::println);
        }
    }

    public void generateTopResearcherNews() {
        Database.getInstance().generateTopResearcherNews();
        LogManager.getInstance().log(getUsername(), "Generated top researcher news");
        System.out.println(Messages.get("admin.top_researcher.generated"));
    }

    public void createUserInteractive() {
        System.out.println(Messages.get("admin.create.type"));
        int type = readInt();
        System.out.print(Messages.get("admin.create.firstname")); String fn = scanner.nextLine().trim();
        System.out.print(Messages.get("admin.create.lastname"));  String ln = scanner.nextLine().trim();
        System.out.print(Messages.get("admin.create.username"));   String un = scanner.nextLine().trim();
        System.out.print(Messages.get("admin.create.password"));   String pw = scanner.nextLine().trim();
        System.out.print(Messages.get("admin.create.email"));      String em = scanner.nextLine().trim();
        System.out.print(Messages.get("admin.create.gender"));
        Gender g = scanner.nextLine().trim().equalsIgnoreCase("FEMALE") ? Gender.FEMALE : Gender.MALE;
        System.out.print(Messages.get("admin.create.faculty"));
        Faculty fac;
        try { fac = Faculty.valueOf(scanner.nextLine().trim().toUpperCase()); }
        catch (IllegalArgumentException e) { fac = Faculty.SITE; }

        switch (type) {
            case 1 -> {
                System.out.print(Messages.get("admin.create.degree"));
                DegreeType dt;
                try { dt = DegreeType.valueOf(scanner.nextLine().trim().toUpperCase()); }
                catch (IllegalArgumentException e) { dt = DegreeType.BACHELOR; }
                System.out.print(Messages.get("admin.create.year")); int yr = readInt();
                createStudent(fn, ln, un, pw, g, LocalDate.now(), em, fac, dt, yr);
            }
            case 2 -> {
                System.out.print(Messages.get("admin.create.degree"));
                DegreeType dt;
                try { dt = DegreeType.valueOf(scanner.nextLine().trim().toUpperCase()); }
                catch (IllegalArgumentException e) { dt = DegreeType.MASTER; }
                System.out.print(Messages.get("admin.create.year")); int yr = readInt();
                createGraduateStudent(fn, ln, un, pw, g, LocalDate.now(), em, fac, dt, yr);
            }
            case 3 -> {
                System.out.print(Messages.get("admin.create.salary")); double sal = Double.parseDouble(scanner.nextLine().trim());
                System.out.print(Messages.get("admin.create.teacher_degree")); String deg = scanner.nextLine().trim();
                System.out.print(Messages.get("admin.create.professor")); boolean prof = scanner.nextLine().trim().equalsIgnoreCase("y");
                createTeacher(fn, ln, un, pw, g, LocalDate.now(), em, fac, sal, LocalDate.now(), "INS-NEW", deg, prof);
            }
            case 4 -> {
                System.out.print(Messages.get("admin.create.salary")); double sal = Double.parseDouble(scanner.nextLine().trim());
                System.out.print(Messages.get("admin.create.position"));
                ManagerPosition pos;
                try { pos = ManagerPosition.valueOf(scanner.nextLine().trim().toUpperCase()); }
                catch (IllegalArgumentException e) { pos = ManagerPosition.OR; }
                createManager(fn, ln, un, pw, g, LocalDate.now(), em, fac, sal, LocalDate.now(), "INS-NEW", pos);
            }
            case 5 -> {
                System.out.print(Messages.get("admin.create.salary")); double sal = Double.parseDouble(scanner.nextLine().trim());
                createLibrarian(fn, ln, un, pw, g, LocalDate.now(), em, fac, sal, LocalDate.now(), "INS-NEW");
            }
            case 6 -> {
                System.out.print(Messages.get("admin.create.salary")); double sal = Double.parseDouble(scanner.nextLine().trim());
                createTechSupport(fn, ln, un, pw, g, LocalDate.now(), em, fac, sal, LocalDate.now(), "INS-NEW");
            }
            case 7 -> {
                System.out.print(Messages.get("admin.create.salary")); double sal = Double.parseDouble(scanner.nextLine().trim());
                System.out.print(Messages.get("admin.create.field")); String field = scanner.nextLine().trim();
                createEmployeeResearcher(fn, ln, un, pw, g, LocalDate.now(), em, fac, sal, LocalDate.now(), "INS-NEW", field);
            }
            case 8 -> {
                System.out.print(Messages.get("admin.create.salary")); double sal = Double.parseDouble(scanner.nextLine().trim());
                System.out.print(Messages.get("admin.create.teacher_degree")); String deg = scanner.nextLine().trim();
                createDean(fn, ln, un, pw, g, LocalDate.now(), em, fac, sal, LocalDate.now(), "INS-NEW", deg);
            }
            default -> { System.out.println(Messages.get("admin.create.invalid")); return; }
        }
        System.out.println(Messages.get("admin.create.done"));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("admin.menu.title"));
            System.out.println("1. " + Messages.get("admin.menu.1"));
            System.out.println("2. " + Messages.get("admin.menu.2"));
            System.out.println("3. " + Messages.get("admin.menu.3"));
            System.out.println("4. " + Messages.get("admin.menu.4"));
            System.out.println("5. " + Messages.get("admin.menu.5"));
            System.out.println("6. " + Messages.get("admin.menu.6"));
            System.out.println("7. " + Messages.get("admin.menu.7"));
            System.out.println("8. " + Messages.get("admin.menu.8"));
            System.out.println("9. " + Messages.get("admin.menu.9"));
            System.out.println("10. " + Messages.get("admin.menu.10"));
            System.out.println("11. " + Messages.get("admin.menu.11"));
            System.out.println("12. " + Messages.get("admin.menu.12"));
            System.out.println("13. " + Messages.get("admin.menu.13"));
            System.out.println("14. " + Messages.get("admin.menu.14"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            switch (readInt()) {
                case 1  -> viewAllUsers();
                case 2  -> createUserInteractive();
                case 3  -> { System.out.print(Messages.get("admin.users.remove_prompt")); removeUser(scanner.nextLine().trim()); }
                case 4  -> addNewsInteractive();
                case 5  -> editNewsInteractive();
                case 6  -> deleteNewsInteractive();
                case 7  -> viewNews();
                case 8  -> viewLogs();
                case 9  -> viewLogsForUser();
                case 10 -> generateTopResearcherNews();
                case 11 -> {
                    System.out.println(Messages.fmt("admin.top_researcher", Database.getInstance().getTopCitedResearcher()));
                    System.out.println(Messages.fmt("admin.top_researcher.by", Faculty.SITE, Database.getInstance().getTopCitedResearcherByFaculty(Faculty.SITE)));
                }
                case 12 -> viewInbox();
                case 13 -> sendMessageInteractive();
                case 14 -> { viewPersonalInfo(); editPersonalInfo(); }
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }
}
