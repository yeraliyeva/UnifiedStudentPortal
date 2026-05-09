package bootstrap;

import domain.user.*;
import presentation.cli.menu.*;

public final class DefaultMenuFactory implements MenuFactory {
    private final AppContext ctx;

    public DefaultMenuFactory(AppContext ctx) { this.ctx = ctx; }

    @Override public Menu menuFor(User user) {
        if (user instanceof Admin a) {
            return new AdminMenu(ctx.console, a, ctx.userRepository, ctx.logRepository,
                    ctx.deleteUser, ctx.generateTopResearcherNews);
        }
        if (user instanceof Manager m) {
            return new ManagerMenu(ctx.console, m, ctx.courseRepository, ctx.requestRepository,
                    ctx.createCourse, ctx.assignTeacher, ctx.addLesson,
                    ctx.setCoursePrerequisite, ctx.setCourseCapacity,
                    ctx.publishNews, ctx.processRequest, ctx.generateAcademicReport);
        }
        if (user instanceof Dean d) {
            return new DeanMenu(ctx.console, d, ctx.requestRepository, ctx.processRequest,
                    ctx.becomeResearcherAction, ctx.researcherMenu);
        }
        if (user instanceof Librarian l) {
            return new LibrarianMenu(ctx.console, l, ctx.bookRepository, ctx.addBook, ctx.removeBook);
        }
        if (user instanceof TechSupport t) {
            return new TechSupportMenu(ctx.console, t, ctx.orderRepository, ctx.acceptOrder, ctx.completeOrder);
        }
        if (user instanceof EmployeeResearcher er) {
            return new EmployeeResearcherMenu(ctx.console, er, ctx.becomeResearcherAction, ctx.researcherMenu);
        }
        if (user instanceof Teacher tch) {
            return new TeacherMenu(ctx.console, tch, ctx.courseRepository, ctx.recordMarks,
                    ctx.complainAboutStudent, ctx.becomeResearcherAction, ctx.researcherMenu);
        }
        if (user instanceof GraduateStudent gs) {
            return new GraduateStudentMenu(ctx.console, gs, ctx.courseRepository, ctx.orgRepository,
                    ctx.projectRepository, ctx.notificationRepository,
                    ctx.enrollInCourse, ctx.dropCourse, ctx.borrowBook, ctx.returnBook,
                    ctx.viewTranscript, ctx.createOrganization, ctx.joinOrganization,
                    ctx.joinResearchProject, ctx.setSupervisor,
                    ctx.becomeResearcherAction, ctx.researcherMenu);
        }
        if (user instanceof Student s) {
            return new StudentMenu(ctx.console, s, ctx.courseRepository, ctx.orgRepository,
                    ctx.notificationRepository, ctx.enrollInCourse, ctx.dropCourse,
                    ctx.borrowBook, ctx.returnBook, ctx.viewTranscript,
                    ctx.rateTeacher, ctx.createOrganization, ctx.joinOrganization,
                    ctx.becomeResearcherAction, ctx.researcherMenu);
        }
        throw new IllegalStateException("No menu for role " + user.getClass().getSimpleName());
    }
}
