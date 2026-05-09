package users;

import communication.Message;
import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import education.Book;
import education.Course;
import enums.Faculty;
import enums.Gender;
import enums.UrgencyLevel;
import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import interfaces.CanBorrowBook;
import interfaces.Researcher;
import interfaces.Subscriber;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * University teacher/lecturer.
 *
 * Can be a Researcher (if professor or self-assigned).
 * Teachers always implement Researcher directly in this design —
 * the system allows any teacher to do research.
 *
 * Design note: removed isAdvisor flag. Graduate student supervision
 * is handled by GraduateStudent holding the supervisor's username.
 */
public class Teacher extends Employee implements Researcher, CanBorrowBook, Subscriber {

    private String degree;       // e.g. "PhD", "MSc"
    private boolean isProfessor;
    private final List<Course> taughtCourses = new ArrayList<>();
    private final List<ResearchPaper> myPapers = new ArrayList<>();
    private final List<ResearchProject> myProjects = new ArrayList<>();
    private final List<Book> borrowedBooks = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();

    public Teacher(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty, double salary,
                   LocalDate hireDate, String insuranceNumber,
                   String degree, boolean isProfessor) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
        this.degree = degree;
        this.isProfessor = isProfessor;
    }

    // ── teaching ──────────────────────────────────────────────────

    public void assignToCourse(Course course) {
        if (!taughtCourses.contains(course)) {
            taughtCourses.add(course);
            course.addTeacher(this);
        }
    }

    public void viewTaughtCourses() {
        System.out.println("\n=== MY COURSES ===");
        if (taughtCourses.isEmpty()) { System.out.println("No courses assigned."); return; }
        taughtCourses.forEach(c -> System.out.println(c + " | Students: " + c.getStudents().size()));
    }

    public void putMarksInteractive() {
        viewTaughtCourses();
        System.out.print("Enter Course ID: ");
        String cid = scanner.nextLine().trim();
        taughtCourses.stream().filter(c -> c.getCourseId().equals(cid)).findFirst().ifPresentOrElse(course -> {
            if (course.getStudents().isEmpty()) { System.out.println("No students enrolled."); return; }
            for (Student s : course.getStudents()) {
                System.out.println("Student: " + s.getUsername());
                System.out.print("Att1 (0-30): "); int a1 = readInt();
                System.out.print("Att2 (0-30): "); int a2 = readInt();
                System.out.print("Exam (0-40): "); int ex = readInt();
                course.setGrade(s.getUsername(), a1, a2, ex);
                System.out.println("Grade saved.");
            }
        }, () -> System.out.println("Course not found or not assigned to you."));
    }

    public void viewStudentAttestations() {
        viewTaughtCourses();
        System.out.print("Enter Course ID: ");
        String cid = scanner.nextLine().trim();
        taughtCourses.stream().filter(c -> c.getCourseId().equals(cid)).findFirst().ifPresentOrElse(course -> {
            System.out.println("Grades for " + course.getCourseName() + ":");
            course.getAllGrades().forEach((username, grade) ->
                    System.out.println("  " + username + ": " + grade));
        }, () -> System.out.println("Course not found."));
    }

    public void viewOfficeHoursSchedule() {
        System.out.println("\n=== OFFICE HOURS ===");
        taughtCourses.forEach(c ->
            c.getLessons().stream()
                .filter(l -> l.getType() == enums.LessonType.OFFICE_HOURS)
                .forEach(l -> System.out.println(c.getCourseName() + ": " + l))
        );
    }

    /** Send a complaint about a student to the dean of this faculty. */
    public void sendComplaintAboutStudent(String studentUsername, String reason, UrgencyLevel urgency) {
        // Find the dean for this faculty
        Database.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Dean && u.getFaculty() == getFaculty())
                .findFirst()
                .ifPresentOrElse(
                        dean -> {
                            sendMessage(dean.getUsername(),
                                    "Complaint about student: " + studentUsername,
                                    reason, urgency);
                            System.out.println("Complaint sent to Dean " + dean.getUsername());
                        },
                        () -> System.out.println("No dean found for faculty " + getFaculty()));
    }

    // ── Researcher ────────────────────────────────────────────────

    @Override
    public List<ResearchPaper> getMyPapers() { return Collections.unmodifiableList(myPapers); }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        myPapers.add(paper);
        System.out.println("Paper added: " + paper.getTitle());
    }

    @Override
    public void createResearchProject(ResearchProject project) {
        myProjects.add(project);
        Database.getInstance().addResearchProject(project);
        System.out.println("Project created: " + project.getJournalName());
    }

    @Override
    public List<ResearchProject> getResearchProjects() { return Collections.unmodifiableList(myProjects); }

    public void viewResearchCabinet() {
        System.out.println("\n=== RESEARCH CABINET ===");
        System.out.println("Papers: " + myPapers.size() + " | H-index: " + calculateHIndex());
        myPapers.forEach(System.out::println);
        System.out.println("Projects: " + myProjects.size());
        myProjects.forEach(System.out::println);
    }

    // ── CanBorrowBook ─────────────────────────────────────────────

    @Override
    public void borrowBook(Book book) {
        if (book.isBorrowed()) { System.out.println("Book already borrowed."); return; }
        book.setBorrowed(true);
        borrowedBooks.add(book);
        System.out.println("Borrowed: " + book.getTitle());
    }

    @Override
    public void returnBook(Book book) {
        if (!borrowedBooks.contains(book)) { System.out.println("You haven't borrowed that book."); return; }
        borrowedBooks.remove(book);
        book.setBorrowed(false);
        System.out.println("Returned: " + book.getTitle());
    }

    // ── Subscriber ────────────────────────────────────────────────

    @Override
    public void notifyNewPaper(String journalName, ResearchPaper paper) {
        String note = "[JOURNAL] New paper in '" + journalName + "': " + paper.getTitle();
        notifications.add(note);
        System.out.println(note);
    }

    // ── menu ──────────────────────────────────────────────────────

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== TEACHER MENU ===
                    1. View my courses
                    2. Put marks
                    3. View student attestations
                    4. View lesson schedule
                    5. View office hours
                    6. Send complaint about student
                    7. View inbox
                    8. Send message
                    9. Research cabinet
                    10. Add research paper
                    11. Create research project
                    12. Print my papers (sorted)
                    13. View news
                    14. Borrow a book
                    15. Personal info
                    16. Create tech order
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1  -> viewTaughtCourses();
                case 2  -> putMarksInteractive();
                case 3  -> viewStudentAttestations();
                case 4  -> viewLessonSchedule();
                case 5  -> viewOfficeHoursSchedule();
                case 6  -> complaintInteractive();
                case 7  -> viewInbox();
                case 8  -> sendMessageInteractive();
                case 9  -> viewResearchCabinet();
                case 10 -> addPaperInteractive();
                case 11 -> createProjectInteractive();
                case 12 -> printPapersInteractive();
                case 13 -> viewNews();
                case 14 -> borrowBookInteractive();
                case 15 -> viewPersonalInfo();
                case 16 -> { System.out.print("Order description: "); addOrder(scanner.nextLine().trim()); }
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewLessonSchedule() {
        System.out.println("\n=== LESSON SCHEDULE ===");
        taughtCourses.forEach(c -> {
            System.out.println("  " + c.getCourseName() + ":");
            c.getLessons().forEach(l -> System.out.println("    " + l));
        });
    }

    private void complaintInteractive() {
        System.out.print("Student username: ");
        String sUsername = scanner.nextLine().trim();
        System.out.print("Reason: ");
        String reason = scanner.nextLine().trim();
        System.out.print("Urgency (LOW/MEDIUM/HIGH): ");
        UrgencyLevel urgency = parseUrgency(scanner.nextLine().trim());
        sendComplaintAboutStudent(sUsername, reason, urgency);
    }

    private void addPaperInteractive() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Journal name: ");
        String journal = scanner.nextLine().trim();
        System.out.print("Abstract: ");
        String wording = scanner.nextLine().trim();
        ResearchPaper paper = new ResearchPaper(title, getUsername(), journal, wording);
        Database.getInstance().findProjectByJournal(journal).ifPresent(proj -> proj.publishPaper(paper));
        addResearchPaper(paper);
    }

    private void createProjectInteractive() {
        System.out.print("Journal name: ");
        String journal = scanner.nextLine().trim();
        System.out.print("Topic: ");
        String topic = scanner.nextLine().trim();
        ResearchProject project = new ResearchProject(journal, topic, getUsername());
        createResearchProject(project);
    }

    private void printPapersInteractive() {
        System.out.println("Sort by: 1.Citations  2.Length  3.Date  4.Title");
        switch (readInt()) {
            case 1 -> printPapers(common.PaperComparators.BY_CITATIONS);
            case 2 -> printPapers(common.PaperComparators.BY_LENGTH);
            case 3 -> printPapers(common.PaperComparators.BY_DATE);
            case 4 -> printPapers(common.PaperComparators.BY_TITLE);
        }
    }

    private void borrowBookInteractive() {
        Database.getInstance().getBooks().stream().filter(b -> !b.isBorrowed()).forEach(System.out::println);
        System.out.print("Book title: ");
        String title = scanner.nextLine().trim();
        Database.getInstance().findBookByTitle(title).ifPresentOrElse(
                this::borrowBook, () -> System.out.println("Not available."));
    }

    // ── getters ───────────────────────────────────────────────────
    public String getDegree() { return degree; }
    public boolean isProfessor() { return isProfessor; }
    public List<Course> getTaughtCourses() { return Collections.unmodifiableList(taughtCourses); }
}
