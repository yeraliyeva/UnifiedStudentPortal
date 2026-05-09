package users;

import communication.Request;
import data.Database;
import education.Book;
import education.Course;
import enums.DegreeType;
import enums.Faculty;
import enums.Gender;
import enums.HelpType;
import enums.UrgencyLevel;
import interfaces.CanBorrowBook;
import interfaces.Educationable;
import interfaces.Subscriber;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an undergraduate or postgraduate student.
 *
 * Implements:
 * - CanBorrowBook   → library borrowing
 * - Educationable   → schedule / attestation viewing
 * - Subscriber      → journal notifications
 */
public class Student extends User implements CanBorrowBook, Educationable, Subscriber {

    private DegreeType degreeType;
    private int studyYear;
    private final List<Course> enrolledCourses = new ArrayList<>();
    private final List<Book> borrowedBooks = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();
    private int availableECTS = 60; // credits available to enroll per year

    public Student(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty, DegreeType degreeType, int studyYear) {
        super(firstName, lastName, username, password, gender, dateOfBirth, email, faculty);
        this.degreeType = degreeType;
        this.studyYear = studyYear;
    }

    // ── course enrollment ─────────────────────────────────────────

    public void enrollCourse(Course course) {
        if (enrolledCourses.contains(course)) {
            System.out.println("Already enrolled in " + course.getCourseName());
            return;
        }
        if (availableECTS < course.getCredits()) {
            System.out.println("Not enough ECTS credits available.");
            return;
        }
        availableECTS -= course.getCredits();
        enrolledCourses.add(course);
        course.enrollStudent(this);
        System.out.println("Enrolled in: " + course.getCourseName());
    }

    public void dropCourse(Course course) {
        if (!enrolledCourses.contains(course)) {
            System.out.println("Not enrolled in " + course.getCourseName());
            return;
        }
        enrolledCourses.remove(course);
        course.removeStudent(this);
        availableECTS += course.getCredits();
        System.out.println("Dropped: " + course.getCourseName());
    }

    public void viewEnrolledCourses() {
        System.out.println("\n=== MY COURSES ===");
        if (enrolledCourses.isEmpty()) { System.out.println("No courses enrolled."); return; }
        enrolledCourses.forEach(System.out::println);
    }

    public void viewAvailableCourses() {
        System.out.println("\n=== AVAILABLE COURSES ===");
        Database.getInstance().getCourses()
                .forEach(c -> System.out.println(c + " | Available ECTS: " + availableECTS));
    }

    // ── grades & transcript ───────────────────────────────────────

    public void viewAttestation() {
        System.out.println("\n=== ATTESTATION RESULTS ===");
        for (Course course : enrolledCourses) {
            var grade = course.getGrade(getUsername());
            if (grade != null) {
                System.out.println(course.getCourseName() + ": " + grade);
            }
        }
    }

    public void viewTranscript() {
        System.out.println("\n=== TRANSCRIPT — " + getFirstName() + " " + getLastName() + " ===");
        System.out.println("Degree: " + degreeType + " | Year: " + studyYear);
        viewAttestation();
        double gpa = calculateGPA();
        System.out.printf("GPA: %.2f%n", gpa);
    }

    private double calculateGPA() {
        if (enrolledCourses.isEmpty()) return 0.0;
        double total = 0;
        int count = 0;
        for (Course c : enrolledCourses) {
            var grade = c.getGrade(getUsername());
            if (grade != null) {
                total += grade.getTotal();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    // ── Educationable ─────────────────────────────────────────────

    @Override
    public void viewLessonSchedule() {
        System.out.println("\n=== LESSON SCHEDULE ===");
        for (Course course : enrolledCourses) {
            System.out.println("  " + course.getCourseName() + ":");
            course.getLessons().forEach(l -> System.out.println("    " + l));
        }
    }

    @Override
    public void viewExamsSchedule() {
        System.out.println("\n=== EXAM SCHEDULE ===");
        enrolledCourses.stream()
                .flatMap(c -> c.getLessons().stream()
                        .filter(l -> l.getType() == enums.LessonType.PRACTICE)
                        .map(l -> c.getCourseName() + ": " + l))
                .forEach(System.out::println);
    }

    @Override
    public void viewJournal() {
        viewAttestation();
    }

    @Override
    public void attendanceMark() {
        System.out.println("Attendance has been marked for " + getUsername());
    }

    @Override
    public void viewDisciplineSchedule() {
        viewLessonSchedule();
    }

    // ── CanBorrowBook ─────────────────────────────────────────────

    @Override
    public void borrowBook(Book book) {
        if (book.isBorrowed()) {
            System.out.println("Book is already borrowed.");
            return;
        }
        book.setBorrowed(true);
        borrowedBooks.add(book);
        System.out.println("Borrowed: " + book.getTitle());
    }

    @Override
    public void returnBook(Book book) {
        if (!borrowedBooks.contains(book)) {
            System.out.println("You haven't borrowed that book.");
            return;
        }
        borrowedBooks.remove(book);
        book.setBorrowed(false);
        System.out.println("Returned: " + book.getTitle());
    }

    // ── Subscriber ────────────────────────────────────────────────

    @Override
    public void notifyNewPaper(String journalName, communication.ResearchPaper paper) {
        String note = "[JOURNAL NOTIFICATION] New paper in '" + journalName + "': " + paper.getTitle();
        notifications.add(note);
        System.out.println(note);
    }

    public void viewNotifications() {
        System.out.println("\n=== NOTIFICATIONS ===");
        if (notifications.isEmpty()) { System.out.println("No notifications."); return; }
        notifications.forEach(System.out::println);
    }

    // ── menu ──────────────────────────────────────────────────────

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== STUDENT MENU ===
                    1. View enrolled courses
                    2. View available courses & enroll
                    3. Drop a course
                    4. View transcript
                    5. View lesson schedule
                    6. View inbox
                    7. Send message
                    8. Submit request
                    9. View my requests
                    10. View news
                    11. View notifications
                    12. Borrow a book
                    13. Return a book
                    14. Personal info
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1  -> viewEnrolledCourses();
                case 2  -> enrollInteractive();
                case 3  -> dropInteractive();
                case 4  -> viewTranscript();
                case 5  -> viewLessonSchedule();
                case 6  -> viewInbox();
                case 7  -> sendMessageInteractive();
                case 8  -> submitRequestInteractive();
                case 9  -> viewMyRequests();
                case 10 -> viewNews();
                case 11 -> viewNotifications();
                case 12 -> borrowBookInteractive();
                case 13 -> returnBookInteractive();
                case 14 -> viewPersonalInfo();
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // ── interactive helpers ───────────────────────────────────────

    private void enrollInteractive() {
        viewAvailableCourses();
        System.out.print("Enter Course ID to enroll: ");
        String id = scanner.nextLine().trim();
        Database.getInstance().findCourseById(id).ifPresentOrElse(
                this::enrollCourse,
                () -> System.out.println("Course not found."));
    }

    private void dropInteractive() {
        viewEnrolledCourses();
        System.out.print("Enter Course ID to drop: ");
        String id = scanner.nextLine().trim();
        enrolledCourses.stream()
                .filter(c -> c.getCourseId().equals(id))
                .findFirst()
                .ifPresentOrElse(this::dropCourse, () -> System.out.println("Not enrolled in that course."));
    }

    private void submitRequestInteractive() {
        System.out.println("Request types:");
        for (HelpType t : HelpType.values()) System.out.println("  " + t.ordinal() + ". " + t);
        System.out.print("Choose type number: ");
        int idx = readInt();
        HelpType type = HelpType.values()[Math.min(idx, HelpType.values().length - 1)];
        System.out.print("Additional info: ");
        String info = scanner.nextLine().trim();
        System.out.print("Urgency (LOW/MEDIUM/HIGH): ");
        UrgencyLevel urgency = parseUrgency(scanner.nextLine().trim());
        submitRequest(type, urgency, info);
    }

    private void borrowBookInteractive() {
        System.out.println("Available books:");
        Database.getInstance().getBooks().stream()
                .filter(b -> !b.isBorrowed())
                .forEach(System.out::println);
        System.out.print("Enter book title: ");
        String title = scanner.nextLine().trim();
        Database.getInstance().findBookByTitle(title).ifPresentOrElse(
                this::borrowBook,
                () -> System.out.println("Book not available."));
    }

    private void returnBookInteractive() {
        System.out.println("Your borrowed books:");
        borrowedBooks.forEach(System.out::println);
        System.out.print("Enter book title to return: ");
        String title = scanner.nextLine().trim();
        borrowedBooks.stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(this::returnBook, () -> System.out.println("Book not found in your list."));
    }

    // ── getters / setters ─────────────────────────────────────────

    public DegreeType getDegreeType() { return degreeType; }
    public void setDegreeType(DegreeType degreeType) { this.degreeType = degreeType; }
    public int getStudyYear() { return studyYear; }
    public void setStudyYear(int studyYear) { this.studyYear = studyYear; }
    public List<Course> getEnrolledCourses() { return Collections.unmodifiableList(enrolledCourses); }
    public int getAvailableECTS() { return availableECTS; }

}
