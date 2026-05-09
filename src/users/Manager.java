package users;

import communication.Request;
import data.Database;
import education.Course;
import education.Lesson;
import enums.*;
import interfaces.Managable;

import java.time.LocalDate;
import java.util.List;

/**
 * Manager user. Behaviour differs by ManagerPosition:
 *  - OR          → handles student academic requests (transcripts, mobility, etc.)
 *  - DEANS_OFFICE → forwards approved requests to the Dean
 *  - DEPARTMENT  → organizational/department requests
 *
 * Also handles course management: create courses, add/edit, assign teachers.
 */
public class Manager extends Employee implements Managable {

    private ManagerPosition position;

    public Manager(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty, double salary,
                   LocalDate hireDate, String insuranceNumber,
                   ManagerPosition position) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
        this.position = position;
    }

    // ── Managable ─────────────────────────────────────────────────

    @Override
    public void viewAcademicStatistics() {
        System.out.println("\n=== ACADEMIC STATISTICS ===");
        Database.getInstance().getCourses().stream()
                .filter(c -> c.getTeachers().stream()
                        .anyMatch(t -> t.getFaculty() == getFaculty()))
                .forEach(c -> {
                    System.out.println(c + " | Students: " + c.getStudents().size());
                    c.getAllGrades().forEach((u, g) ->
                            System.out.println("    " + u + ": " + g));
                });
    }

    // ── requests ──────────────────────────────────────────────────

    public void viewRequests() {
        System.out.println("\n=== REQUESTS FOR PROCESSING (" + position + ") ===");
        Database.getInstance().getAllRequests().stream()
                .filter(r -> matchesPosition(r) && r.getStatus() == RequestStatus.PENDING)
                .forEach(System.out::println);
    }

    private boolean matchesPosition(Request r) {
        return switch (position) {
            case OR -> r.getType() == HelpType.TRANSCRIPT_FOR_SEMESTER
                    || r.getType() == HelpType.TRANSCRIPT_FOR_YEAR
                    || r.getType() == HelpType.TRANSCRIPT_FOR_ENTIRE_STUDY
                    || r.getType() == HelpType.CERTIFICATE_OF_EDUCATION
                    || r.getType() == HelpType.ACADEMIC_MOBILITY;
            case DEANS_OFFICE -> r.getType() == HelpType.COORDINATION_OF_DIPLOMA_TOPIC
                    || r.getType() == HelpType.WORKAROUND_SHEET
                    || r.getType() == HelpType.REQUEST_FOR_CREATING_ORGANIZATION;
            case DEPARTMENT -> r.getType() == HelpType.RESTORING_ONAY_CARD
                    || r.getType() == HelpType.HELP_DEPARTMENT_OF_DEFENSE
                    || r.getType() == HelpType.HELP_LARGE_FAMILIES
                    || r.getType() == HelpType.HELP_LOSS_OF_BREADWINNER
                    || r.getType() == HelpType.HELP_FINANCING_KAZENERGY
                    || r.getType() == HelpType.INFORMATION_ABOUT_PLACE_OF_REQUIREMENT;
        };
    }

    public void processRequestInteractive() {
        viewRequests();
        System.out.print("Enter Request ID: ");
        int id = readInt();
        Database.getInstance().findRequestById(id).ifPresentOrElse(req -> {
            System.out.println("1. Accept   2. Reject");
            int choice = readInt();
            if (choice == 1) {
                req.setStatus(RequestStatus.ACCEPTED);
                System.out.println("Request accepted. Forwarded to Dean if applicable.");
                // Notify requester
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Accepted",
                        "Your request has been accepted and is being processed.",
                        UrgencyLevel.MEDIUM);
            } else if (choice == 2) {
                req.setStatus(RequestStatus.NOT_APPROVED);
                System.out.println("Request rejected.");
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Not Approved",
                        "Your request was rejected at the manager level.",
                        UrgencyLevel.MEDIUM);
            }
        }, () -> System.out.println("Request not found."));
    }

    // ── course management ─────────────────────────────────────────

    public void createCourseInteractive() {
        System.out.print("Course name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Credits: ");
        int credits = readInt();
        System.out.println("Type: 1.MAJOR  2.MINOR  3.FREE");
        DisciplineType type = switch (readInt()) {
            case 2 -> DisciplineType.MINOR;
            case 3 -> DisciplineType.FREE;
            default -> DisciplineType.MAJOR;
        };
        Course course = new Course(name, credits, type);
        Database.getInstance().addCourse(course);
        System.out.println("Course created: " + course);
    }

    public void viewAllCourses() {
        System.out.println("\n=== ALL COURSES ===");
        Database.getInstance().getCourses().forEach(System.out::println);
    }

    public void assignTeacherToCourseInteractive() {
        viewAllCourses();
        System.out.print("Course ID: ");
        String cid = scanner.nextLine().trim();
        Database.getInstance().findCourseById(cid).ifPresentOrElse(course -> {
            System.out.print("Teacher username: ");
            String tUsername = scanner.nextLine().trim();
            User u = Database.getInstance().getUser(tUsername);
            if (u instanceof Teacher teacher) {
                teacher.assignToCourse(course);
                System.out.println("Teacher assigned.");
            } else {
                System.out.println("User not found or not a teacher.");
            }
        }, () -> System.out.println("Course not found."));
    }

    public void addLessonToCourseInteractive() {
        viewAllCourses();
        System.out.print("Course ID: ");
        String cid = scanner.nextLine().trim();
        Database.getInstance().findCourseById(cid).ifPresentOrElse(course -> {
            System.out.println("Lesson type: 1.LECTURE  2.PRACTICE  3.OFFICE_HOURS");
            LessonType type = switch (readInt()) {
                case 2 -> LessonType.PRACTICE;
                case 3 -> LessonType.OFFICE_HOURS;
                default -> LessonType.LECTURE;
            };
            System.out.println("Day (MONDAY..SUNDAY): ");
            WeekDay day;
            try { day = WeekDay.valueOf(scanner.nextLine().trim().toUpperCase()); }
            catch (Exception e) { day = WeekDay.MONDAY; }
            System.out.print("Time (e.g. 09:00): ");
            String time = scanner.nextLine().trim();
            System.out.print("Room: ");
            String room = scanner.nextLine().trim();
            course.addLesson(new Lesson(type, day, time, room));
            System.out.println("Lesson added.");
        }, () -> System.out.println("Course not found."));
    }

    // ── viewing schedules ─────────────────────────────────────────

    public void viewStudentSchedule() {
        System.out.print("Student username: ");
        String uname = scanner.nextLine().trim();
        User u = Database.getInstance().getUser(uname);
        if (u instanceof Student student) {
            student.viewLessonSchedule();
        } else {
            System.out.println("Student not found.");
        }
    }

    // ── menu ──────────────────────────────────────────────────────

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== MANAGER MENU [" + position + "] ===" + """
                    \n1. View requests
                    2. Process a request
                    3. Academic statistics
                    4. Create course
                    5. View all courses
                    6. Assign teacher to course
                    7. Add lesson to course
                    8. View student schedule
                    9. View inbox
                    10. Send message
                    11. View news
                    12. Personal info
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1  -> viewRequests();
                case 2  -> processRequestInteractive();
                case 3  -> viewAcademicStatistics();
                case 4  -> createCourseInteractive();
                case 5  -> viewAllCourses();
                case 6  -> assignTeacherToCourseInteractive();
                case 7  -> addLessonToCourseInteractive();
                case 8  -> viewStudentSchedule();
                case 9  -> viewInbox();
                case 10 -> sendMessageInteractive();
                case 11 -> viewNews();
                case 12 -> viewPersonalInfo();
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    public ManagerPosition getPosition() { return position; }
    public void setPosition(ManagerPosition position) { this.position = position; }
}
