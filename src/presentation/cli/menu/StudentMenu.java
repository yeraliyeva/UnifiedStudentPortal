package presentation.cli.menu;

import application.Result;
import application.usecase.course.DropCourse;
import application.usecase.course.EnrollInCourse;
import application.usecase.course.ViewTranscript;
import application.usecase.library.BorrowBook;
import application.usecase.library.ReturnBook;
import application.usecase.organization.CreateOrganization;
import application.usecase.organization.JoinOrganization;
import application.usecase.user.RateTeacher;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.repository.NotificationRepository;
import domain.repository.OrganizationRepository;
import domain.shared.Username;
import domain.user.Student;
import presentation.cli.Console;

import java.util.ArrayList;
import java.util.List;

public final class StudentMenu extends Menu {
    private final Student student;
    private final CourseRepository courses;
    private final OrganizationRepository orgs;
    private final NotificationRepository notifications;
    private final EnrollInCourse enroll;
    private final DropCourse drop;
    private final BorrowBook borrow;
    private final ReturnBook returnBook;
    private final ViewTranscript viewTranscript;
    private final RateTeacher rateTeacher;
    private final CreateOrganization createOrg;
    private final JoinOrganization joinOrg;
    private final BecomeResearcherAction becomeResearcher;
    private final ResearcherMenuExtension researcherMenu;

    public StudentMenu(Console console, Student student,
                       CourseRepository courses, OrganizationRepository orgs,
                       NotificationRepository notifications,
                       EnrollInCourse enroll, DropCourse drop,
                       BorrowBook borrow, ReturnBook returnBook,
                       ViewTranscript viewTranscript,
                       RateTeacher rateTeacher,
                       CreateOrganization createOrg, JoinOrganization joinOrg,
                       BecomeResearcherAction becomeResearcher,
                       ResearcherMenuExtension researcherMenu) {
        super(console);
        this.student = student;
        this.courses = courses;
        this.orgs = orgs;
        this.notifications = notifications;
        this.enroll = enroll;
        this.drop = drop;
        this.borrow = borrow;
        this.returnBook = returnBook;
        this.viewTranscript = viewTranscript;
        this.rateTeacher = rateTeacher;
        this.createOrg = createOrg;
        this.joinOrg = joinOrg;
        this.becomeResearcher = becomeResearcher;
        this.researcherMenu = researcherMenu;
    }

    @Override protected String title() { return "=== STUDENT MENU (" + student.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        List<MenuItem> items = new ArrayList<>();
        if (!student.isResearcher()) {
            items.add(new MenuItem("Become a researcher", () -> becomeResearcher.run(student)));
        }
        items.add(new MenuItem("View enrolled courses", this::viewEnrolled));
        items.add(new MenuItem("View available courses", this::viewAvailable));
        items.add(new MenuItem("Enroll in a course", this::enrollInteractive));
        items.add(new MenuItem("Drop a course", this::dropInteractive));
        items.add(new MenuItem("View transcript", this::renderTranscript));
        items.add(new MenuItem("Borrow a book", this::borrowInteractive));
        items.add(new MenuItem("Return a book", this::returnInteractive));
        items.add(new MenuItem("View notifications", this::viewNotifications));
        items.add(new MenuItem("Rate a teacher", this::rateInteractive));
        items.add(new MenuItem("View organizations", this::listOrgs));
        items.add(new MenuItem("Join organization", this::joinOrgInteractive));
        items.add(new MenuItem("Create organization", this::createOrgInteractive));
        items.addAll(researcherMenu.itemsFor(student));
        return items;
    }

    private void viewEnrolled() {
        if (student.enrolledCourses().isEmpty()) { console.println("No enrollments."); return; }
        student.enrolledCourses().forEach(id -> courses.findById(id).ifPresent(c -> console.println("  " + c)));
    }

    private void viewAvailable() {
        if (courses.findAll().isEmpty()) { console.println("No courses."); return; }
        courses.findAll().forEach(c -> console.println("  " + c));
    }

    private void enrollInteractive() {
        viewAvailable();
        Result r = enroll.execute(student, new CourseId(console.readLine("Course ID:")));
        console.println(r.message());
    }

    private void dropInteractive() {
        viewEnrolled();
        Result r = drop.execute(student, new CourseId(console.readLine("Course ID:")));
        console.println(r.message());
    }

    private void renderTranscript() {
        ViewTranscript.Transcript t = viewTranscript.execute(student);
        console.println("\n=== TRANSCRIPT — " + t.fullName() + " ===");
        console.println("Degree: " + t.degreeType() + " | Year: " + t.year() + " | Fails: " + t.failCount());
        t.lines().forEach(l -> console.println("  " + l.courseName() + ": " + l.letter() + " (" + l.total() + ")"));
        console.println(String.format("GPA: %.2f", t.gpa()));
    }

    private void borrowInteractive() {
        Result r = borrow.execute(student, console.readLine("Book title:"));
        console.println(r.message());
    }

    private void returnInteractive() {
        Result r = returnBook.execute(student, console.readLine("Book title:"));
        console.println(r.message());
    }

    private void viewNotifications() {
        var list = notifications.findFor(student.username());
        if (list.isEmpty()) { console.println("No notifications."); return; }
        list.forEach(n -> console.println(n.toString()));
    }

    private void rateInteractive() {
        String teacher = console.readLine("Teacher username:");
        int rating = console.readInt("Rating (1-10):");
        Result r = rateTeacher.execute(student, new Username(teacher), rating);
        console.println(r.message());
    }

    private void listOrgs() {
        if (orgs.findAll().isEmpty()) { console.println("No organizations."); return; }
        orgs.findAll().forEach(o -> console.println("  " + o));
    }

    private void joinOrgInteractive() {
        Result r = joinOrg.execute(student, console.readLine("Organization name:"));
        console.println(r.message());
    }

    private void createOrgInteractive() {
        Result r = createOrg.execute(student, console.readLine("Organization name:"));
        console.println(r.message());
    }
}
