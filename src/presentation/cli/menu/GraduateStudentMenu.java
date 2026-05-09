package presentation.cli.menu;

import application.Result;
import application.usecase.course.DropCourse;
import application.usecase.course.EnrollInCourse;
import application.usecase.course.ViewTranscript;
import application.usecase.library.BorrowBook;
import application.usecase.library.ReturnBook;
import application.usecase.organization.CreateOrganization;
import application.usecase.organization.JoinOrganization;
import application.usecase.research.JoinResearchProject;
import application.usecase.research.SetSupervisor;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.repository.NotificationRepository;
import domain.repository.OrganizationRepository;
import domain.repository.ResearchProjectRepository;
import domain.shared.Username;
import domain.user.GraduateStudent;
import presentation.cli.Console;

import java.util.ArrayList;
import java.util.List;

public final class GraduateStudentMenu extends Menu {
    private final GraduateStudent grad;
    private final CourseRepository courses;
    private final OrganizationRepository orgs;
    private final ResearchProjectRepository projects;
    private final NotificationRepository notifications;
    private final EnrollInCourse enroll;
    private final DropCourse drop;
    private final BorrowBook borrow;
    private final ReturnBook returnBook;
    private final ViewTranscript viewTranscript;
    private final CreateOrganization createOrg;
    private final JoinOrganization joinOrg;
    private final JoinResearchProject joinProject;
    private final SetSupervisor setSupervisor;
    private final BecomeResearcherAction becomeResearcher;
    private final ResearcherMenuExtension researcherMenu;

    public GraduateStudentMenu(Console console, GraduateStudent grad,
                               CourseRepository courses, OrganizationRepository orgs,
                               ResearchProjectRepository projects, NotificationRepository notifications,
                               EnrollInCourse enroll, DropCourse drop,
                               BorrowBook borrow, ReturnBook returnBook,
                               ViewTranscript viewTranscript,
                               CreateOrganization createOrg, JoinOrganization joinOrg,
                               JoinResearchProject joinProject, SetSupervisor setSupervisor,
                               BecomeResearcherAction becomeResearcher,
                               ResearcherMenuExtension researcherMenu) {
        super(console);
        this.grad = grad;
        this.courses = courses;
        this.orgs = orgs;
        this.projects = projects;
        this.notifications = notifications;
        this.enroll = enroll;
        this.drop = drop;
        this.borrow = borrow;
        this.returnBook = returnBook;
        this.viewTranscript = viewTranscript;
        this.createOrg = createOrg;
        this.joinOrg = joinOrg;
        this.joinProject = joinProject;
        this.setSupervisor = setSupervisor;
        this.becomeResearcher = becomeResearcher;
        this.researcherMenu = researcherMenu;
    }

    @Override protected String title() { return "=== GRADUATE STUDENT MENU (" + grad.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        List<MenuItem> items = new ArrayList<>();
        if (!grad.isResearcher()) {
            items.add(new MenuItem("Become a researcher", () -> becomeResearcher.run(grad)));
        }
        items.add(new MenuItem("View enrolled courses", this::viewEnrolled));
        items.add(new MenuItem("View available courses", this::viewAvailable));
        items.add(new MenuItem("Enroll in a course", this::enrollInteractive));
        items.add(new MenuItem("Drop a course", this::dropInteractive));
        items.add(new MenuItem("View transcript", this::renderTranscript));
        items.add(new MenuItem("Borrow a book", this::borrowInteractive));
        items.add(new MenuItem("Return a book", this::returnInteractive));
        items.add(new MenuItem("View notifications", this::viewNotifications));
        items.add(new MenuItem("View organizations", this::listOrgs));
        items.add(new MenuItem("Join organization", this::joinOrgInteractive));
        items.add(new MenuItem("Create organization", this::createOrgInteractive));
        items.addAll(researcherMenu.itemsFor(grad));
        items.add(new MenuItem("Join an existing research project", this::joinProjectInteractive));
        items.add(new MenuItem("Set supervisor", this::setSupervisorInteractive));
        return items;
    }

    private void viewEnrolled() {
        if (grad.enrolledCourses().isEmpty()) { console.println("No enrollments."); return; }
        grad.enrolledCourses().forEach(id -> courses.findById(id).ifPresent(c -> console.println("  " + c)));
    }

    private void viewAvailable() {
        if (courses.findAll().isEmpty()) { console.println("No courses."); return; }
        courses.findAll().forEach(c -> console.println("  " + c));
    }

    private void enrollInteractive() {
        viewAvailable();
        Result r = enroll.execute(grad, new CourseId(console.readLine("Course ID:")));
        console.println(r.message());
    }

    private void dropInteractive() {
        viewEnrolled();
        Result r = drop.execute(grad, new CourseId(console.readLine("Course ID:")));
        console.println(r.message());
    }

    private void renderTranscript() {
        ViewTranscript.Transcript t = viewTranscript.execute(grad);
        console.println("\n=== TRANSCRIPT — " + t.fullName() + " ===");
        console.println("Degree: " + t.degreeType() + " | Year: " + t.year() + " | Fails: " + t.failCount());
        t.lines().forEach(l -> console.println("  " + l.courseName() + ": " + l.letter() + " (" + l.total() + ")"));
        console.println(String.format("GPA: %.2f", t.gpa()));
    }

    private void borrowInteractive() {
        Result r = borrow.execute(grad, console.readLine("Book title:"));
        console.println(r.message());
    }

    private void returnInteractive() {
        Result r = returnBook.execute(grad, console.readLine("Book title:"));
        console.println(r.message());
    }

    private void viewNotifications() {
        var list = notifications.findFor(grad.username());
        if (list.isEmpty()) { console.println("No notifications."); return; }
        list.forEach(n -> console.println(n.toString()));
    }

    private void listOrgs() {
        if (orgs.findAll().isEmpty()) { console.println("No organizations."); return; }
        orgs.findAll().forEach(o -> console.println("  " + o));
    }

    private void joinOrgInteractive() {
        Result r = joinOrg.execute(grad, console.readLine("Organization name:"));
        console.println(r.message());
    }

    private void createOrgInteractive() {
        Result r = createOrg.execute(grad, console.readLine("Organization name:"));
        console.println(r.message());
    }

    private void joinProjectInteractive() {
        if (projects.findAll().isEmpty()) { console.println("No projects."); return; }
        projects.findAll().forEach(p -> console.println("  " + p));
        Result r = joinProject.execute(grad, console.readLine("Journal name:"));
        console.println(r.message());
    }

    private void setSupervisorInteractive() {
        String candidate = console.readLine("Supervisor username:");
        Result r = setSupervisor.execute(grad, new Username(candidate));
        console.println(r.message());
    }
}
