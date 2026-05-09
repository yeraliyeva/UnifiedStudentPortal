package presentation.cli.menu;

import application.Result;
import application.usecase.course.RecordMarks;
import application.usecase.user.ComplainAboutStudent;
import domain.course.CourseId;
import domain.course.Grade;
import domain.enums.UrgencyLevel;
import domain.repository.CourseRepository;
import domain.shared.Username;
import domain.user.Teacher;
import presentation.cli.Console;

import java.util.ArrayList;
import java.util.List;

public final class TeacherMenu extends Menu {
    private final Teacher teacher;
    private final CourseRepository courses;
    private final RecordMarks recordMarks;
    private final ComplainAboutStudent complainAboutStudent;
    private final BecomeResearcherAction becomeResearcher;
    private final ResearcherMenuExtension researcherMenu;

    public TeacherMenu(Console console, Teacher teacher, CourseRepository courses, RecordMarks recordMarks,
                       ComplainAboutStudent complainAboutStudent,
                       BecomeResearcherAction becomeResearcher, ResearcherMenuExtension researcherMenu) {
        super(console);
        this.teacher = teacher;
        this.courses = courses;
        this.recordMarks = recordMarks;
        this.complainAboutStudent = complainAboutStudent;
        this.becomeResearcher = becomeResearcher;
        this.researcherMenu = researcherMenu;
    }

    @Override protected String title() { return "=== TEACHER MENU (" + teacher.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        List<MenuItem> items = new ArrayList<>();
        if (!teacher.isResearcher()) {
            items.add(new MenuItem("Become a researcher", () -> becomeResearcher.run(teacher)));
        }
        items.add(new MenuItem("View my courses", this::viewTaught));
        items.add(new MenuItem("Record student marks", this::recordMarksInteractive));
        items.add(new MenuItem("View my ratings", this::viewRatings));
        items.add(new MenuItem("Send complaint about student", this::complainInteractive));
        items.addAll(researcherMenu.itemsFor(teacher));
        return items;
    }

    private void viewTaught() {
        if (teacher.taughtCourses().isEmpty()) { console.println("No assignments."); return; }
        teacher.taughtCourses().forEach(id -> courses.findById(id).ifPresent(c -> console.println("  " + c)));
    }

    private void recordMarksInteractive() {
        viewTaught();
        String courseId = console.readLine("Course ID:");
        String studentName = console.readLine("Student username:");
        int a1 = console.readInt("Att1 (0-30):");
        int a2 = console.readInt("Att2 (0-30):");
        int ex = console.readInt("Exam (0-40):");
        Result r = recordMarks.execute(teacher, new CourseId(courseId), new Username(studentName), new Grade(a1, a2, ex));
        console.println(r.message());
    }

    private void viewRatings() {
        console.println("Ratings: " + teacher.ratings());
        console.println(String.format("Average: %.1f / 10", teacher.averageRating()));
    }

    private void complainInteractive() {
        String student = console.readLine("Student username:");
        String reason = console.readLine("Reason:");
        String urgRaw = console.readLine("Urgency (LOW/MEDIUM/HIGH):").toUpperCase();
        UrgencyLevel urgency = switch (urgRaw) {
            case "HIGH" -> UrgencyLevel.HIGH;
            case "LOW"  -> UrgencyLevel.LOW;
            default     -> UrgencyLevel.MEDIUM;
        };
        Result r = complainAboutStudent.execute(teacher, new Username(student), reason, urgency);
        console.println(r.message());
    }
}
