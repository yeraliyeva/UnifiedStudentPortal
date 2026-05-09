package presentation.cli.menu;

import application.Result;
import application.usecase.admin.GenerateAcademicReport;
import application.usecase.course.AddLesson;
import application.usecase.course.AssignTeacher;
import application.usecase.course.CreateCourse;
import application.usecase.course.SetCourseCapacity;
import application.usecase.course.SetCoursePrerequisite;
import application.usecase.messaging.ProcessRequest;
import application.usecase.messaging.PublishNews;
import domain.course.CourseId;
import domain.course.Room;
import domain.course.TimeSlot;
import domain.enums.DisciplineType;
import domain.enums.LessonType;
import domain.enums.RequestStatus;
import domain.enums.WeekDay;
import domain.repository.CourseRepository;
import domain.repository.RequestRepository;
import domain.shared.Username;
import domain.user.Manager;
import presentation.cli.Console;

import java.util.List;

public final class ManagerMenu extends Menu {
    private final Manager manager;
    private final CourseRepository courses;
    private final RequestRepository requests;
    private final CreateCourse createCourse;
    private final AssignTeacher assignTeacher;
    private final AddLesson addLesson;
    private final SetCoursePrerequisite setPrereq;
    private final SetCourseCapacity setCapacity;
    private final PublishNews publishNews;
    private final ProcessRequest processRequest;
    private final GenerateAcademicReport report;

    public ManagerMenu(Console console, Manager manager,
                       CourseRepository courses, RequestRepository requests,
                       CreateCourse createCourse, AssignTeacher assignTeacher, AddLesson addLesson,
                       SetCoursePrerequisite setPrereq, SetCourseCapacity setCapacity,
                       PublishNews publishNews, ProcessRequest processRequest,
                       GenerateAcademicReport report) {
        super(console);
        this.manager = manager;
        this.courses = courses;
        this.requests = requests;
        this.createCourse = createCourse;
        this.assignTeacher = assignTeacher;
        this.addLesson = addLesson;
        this.setPrereq = setPrereq;
        this.setCapacity = setCapacity;
        this.publishNews = publishNews;
        this.processRequest = processRequest;
        this.report = report;
    }

    @Override protected String title() { return "=== MANAGER MENU (" + manager.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        return List.of(
                new MenuItem("View all courses", this::viewCourses),
                new MenuItem("Create course", this::createInteractive),
                new MenuItem("Assign teacher to course", this::assignInteractive),
                new MenuItem("Add lesson to course", this::addLessonInteractive),
                new MenuItem("Set course prerequisite", this::setPrereqInteractive),
                new MenuItem("Set course capacity", this::setCapacityInteractive),
                new MenuItem("View pending requests", this::viewPendingRequests),
                new MenuItem("Process a request", this::processRequestInteractive),
                new MenuItem("Publish news", this::publishNewsInteractive),
                new MenuItem("Generate academic report", this::renderReport)
        );
    }

    private void viewCourses() {
        if (courses.findAll().isEmpty()) { console.println("No courses."); return; }
        courses.findAll().forEach(c -> console.println("  " + c));
    }

    private void createInteractive() {
        String name = console.readLine("Course name:");
        int credits = console.readInt("Credits:");
        String typeRaw = console.readLine("Type (MAJOR/MINOR/FREE):").toUpperCase();
        DisciplineType type = switch (typeRaw) {
            case "MINOR" -> DisciplineType.MINOR;
            case "FREE"  -> DisciplineType.FREE;
            default      -> DisciplineType.MAJOR;
        };
        int max = console.readInt("Max students:");
        var c = createCourse.execute(manager.username(), name, Math.max(1, credits), type, Math.max(1, max));
        console.println("Created: " + c);
    }

    private void assignInteractive() {
        viewCourses();
        String cid = console.readLine("Course ID:");
        String teacher = console.readLine("Teacher username:");
        Result r = assignTeacher.execute(manager.username(), new CourseId(cid), new Username(teacher));
        console.println(r.message());
    }

    private void addLessonInteractive() {
        viewCourses();
        String cid = console.readLine("Course ID:");
        String typeRaw = console.readLine("Type (LECTURE/PRACTICE/OFFICE_HOURS/EXAM):").toUpperCase();
        LessonType type = switch (typeRaw) {
            case "PRACTICE"     -> LessonType.PRACTICE;
            case "OFFICE_HOURS" -> LessonType.OFFICE_HOURS;
            case "EXAM"         -> LessonType.EXAM;
            default             -> LessonType.LECTURE;
        };
        WeekDay day;
        try { day = WeekDay.valueOf(console.readLine("Day (MONDAY..SUNDAY):").toUpperCase()); }
        catch (IllegalArgumentException e) { day = WeekDay.MONDAY; }
        String time = console.readLine("Time (HH:MM):");
        String room = console.readLine("Room:");
        Result r = addLesson.execute(manager.username(), new CourseId(cid), type, new TimeSlot(day, time), new Room(room));
        console.println(r.message());
    }

    private void setPrereqInteractive() {
        viewCourses();
        String t = console.readLine("Course ID requiring prereq:");
        String p = console.readLine("Prereq course ID:");
        Result r = setPrereq.execute(manager.username(), new CourseId(t), new CourseId(p));
        console.println(r.message());
    }

    private void setCapacityInteractive() {
        viewCourses();
        String cid = console.readLine("Course ID:");
        int cap = console.readInt("New capacity:");
        Result r = setCapacity.execute(manager.username(), new CourseId(cid), Math.max(1, cap));
        console.println(r.message());
    }

    private void viewPendingRequests() {
        var pending = requests.findAll().stream()
                .filter(r -> r.status() == RequestStatus.PENDING)
                .toList();
        if (pending.isEmpty()) { console.println("No pending requests."); return; }
        pending.forEach(r -> console.println("  " + r));
    }

    private void processRequestInteractive() {
        viewPendingRequests();
        int id = console.readInt("Request ID:");
        String action = console.readLine("Action (1=Accept, 2=Reject):");
        RequestStatus status = "1".equals(action) ? RequestStatus.ACCEPTED : RequestStatus.NOT_APPROVED;
        Result r = processRequest.execute(manager.username(), id, status);
        console.println(r.message());
    }

    private void publishNewsInteractive() {
        String title = console.readLine("Title:");
        String body = console.readLine("Body:");
        Result r = publishNews.execute(manager.username(), title, body);
        console.println(r.message());
    }

    private void renderReport() {
        GenerateAcademicReport.Report r = report.execute(manager.username());
        console.println("\n========== ACADEMIC REPORT ==========");
        console.println("Faculty: " + manager.faculty());
        console.println("Courses: " + r.totalCourses() + " | Students: " + r.totalStudents()
                + " | Teachers: " + r.totalTeachers());
        console.println("\n--- Courses ---");
        r.rows().forEach(row -> console.println(String.format("  %s: enrolled %d/%d, avg %.1f, passing %d",
                row.courseName(), row.enrolled(), row.max(), row.avg(), row.passing())));
        console.println("\n--- Top 5 by GPA ---");
        r.top().forEach(t -> console.println(String.format("  %s — GPA %.2f", t.fullName(), t.gpa())));
    }
}
