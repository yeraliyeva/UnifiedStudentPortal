package test;

import application.Result;
import application.usecase.messaging.CommentOnNews;
import application.usecase.messaging.PublishNews;
import application.usecase.messaging.SendMessage;
import application.usecase.organization.CreateOrganization;
import application.usecase.organization.JoinOrganization;
import application.usecase.research.JoinResearchProject;
import application.usecase.research.SetSupervisor;
import application.usecase.user.BecomeResearcher;
import application.usecase.user.ComplainAboutStudent;
import application.usecase.user.RateTeacher;
import domain.enums.Faculty;
import domain.enums.UrgencyLevel;
import domain.repository.MessageRepository;
import domain.repository.NewsRepository;
import domain.repository.OrganizationRepository;
import domain.repository.RequestRepository;
import domain.repository.ResearchPaperRepository;
import domain.repository.ResearchProjectRepository;
import domain.repository.UserRepository;
import domain.research.JournalName;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.research.ResearchProject;
import domain.service.HIndexCalculator;
import domain.shared.IdSequence;
import domain.shared.Username;
import domain.user.Dean;
import domain.user.GraduateStudent;
import domain.user.Student;
import domain.user.Teacher;
import infrastructure.logging.Logger;
import infrastructure.logging.RepositoryLogger;
import infrastructure.persistence.inmemory.InMemoryLogRepository;
import infrastructure.persistence.inmemory.InMemoryMessageRepository;
import infrastructure.persistence.inmemory.InMemoryNewsRepository;
import infrastructure.persistence.inmemory.InMemoryOrganizationRepository;
import infrastructure.persistence.inmemory.InMemoryRequestRepository;
import infrastructure.persistence.inmemory.InMemoryResearchPaperRepository;
import infrastructure.persistence.inmemory.InMemoryResearchProjectRepository;
import infrastructure.persistence.inmemory.InMemoryUserRepository;
import domain.enums.DegreeType;
import domain.enums.Gender;
import domain.enums.TeacherPosition;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;

import java.time.LocalDate;

public final class NewUseCasesTest {

    public static void runAll() {
        TestRunner.run("RateTeacher: rating in range stored", NewUseCasesTest::testRateInRange);
        TestRunner.run("RateTeacher: rating out of range rejected", NewUseCasesTest::testRateOutOfRange);
        TestRunner.run("RateTeacher: target must be a teacher", NewUseCasesTest::testRateNonTeacher);
        TestRunner.run("ComplainAboutStudent: routed to faculty Dean", NewUseCasesTest::testComplaintRoutes);
        TestRunner.run("ComplainAboutStudent: no Dean -> fail", NewUseCasesTest::testNoDean);
        TestRunner.run("CreateOrganization: stores organization", NewUseCasesTest::testCreateOrg);
        TestRunner.run("JoinOrganization: adds member", NewUseCasesTest::testJoinOrg);
        TestRunner.run("JoinOrganization: already member -> fail", NewUseCasesTest::testJoinOrgTwice);
        TestRunner.run("SetSupervisor: blocks h-index < 3", NewUseCasesTest::testSupervisorLowH);
        TestRunner.run("SetSupervisor: allows h-index >= 3", NewUseCasesTest::testSupervisorHighH);
        TestRunner.run("CommentOnNews: appends comment to news", NewUseCasesTest::testComment);
        TestRunner.run("JoinResearchProject: adds participant", NewUseCasesTest::testJoinProject);
    }

    private static Logger newLogger() { return new RepositoryLogger(new InMemoryLogRepository()); }

    private static Teacher activeTeacher(String username, int citationsPerPaper) {
        Teacher t = Fixtures.teacher(username);
        new BecomeResearcher(newLogger()).execute(t, "AI");
        return t;
    }

    private static void testRateInRange() {
        UserRepository users = new InMemoryUserRepository();
        Teacher t = Fixtures.teacher("bob");
        users.save(t);
        Student s = Fixtures.student("eve");
        Result r = new RateTeacher(users, newLogger()).execute(s, t.username(), 8);
        Assert.isTrue(r.success(), "Rating should succeed");
        Assert.equals(1, t.ratings().size(), "Rating recorded");
    }

    private static void testRateOutOfRange() {
        UserRepository users = new InMemoryUserRepository();
        Teacher t = Fixtures.teacher("bob"); users.save(t);
        Result r = new RateTeacher(users, newLogger()).execute(Fixtures.student("eve"), t.username(), 11);
        Assert.isFalse(r.success(), "Out of range should fail");
    }

    private static void testRateNonTeacher() {
        UserRepository users = new InMemoryUserRepository();
        Student fake = Fixtures.student("not-a-teacher"); users.save(fake);
        Result r = new RateTeacher(users, newLogger()).execute(Fixtures.student("eve"), fake.username(), 8);
        Assert.isFalse(r.success(), "Non-teacher target should fail");
    }

    private static void testComplaintRoutes() {
        UserRepository users = new InMemoryUserRepository();
        MessageRepository messages = new InMemoryMessageRepository();
        SendMessage send = new SendMessage(messages, users, new IdSequence(), newLogger());

        Teacher teacher = Fixtures.teacher("bob");
        Student student = Fixtures.student("eve");
        Dean dean = new Dean(new Username("dean1"), "p", new PersonName("D", "Ean"),
                Gender.MALE, LocalDate.of(1970, 1, 1), new Email("d@e.com"), Faculty.SITE,
                new Money(3000), LocalDate.now(), "INS", "PhD");
        users.save(teacher); users.save(student); users.save(dean);

        Result r = new ComplainAboutStudent(send, users, newLogger())
                .execute(teacher, student.username(), "lazy", UrgencyLevel.HIGH);
        Assert.isTrue(r.success(), "Complaint should succeed");
        Assert.isFalse(messages.inboxOf(dean.username()).isEmpty(), "Dean should receive message");
    }

    private static void testNoDean() {
        UserRepository users = new InMemoryUserRepository();
        MessageRepository messages = new InMemoryMessageRepository();
        Teacher teacher = Fixtures.teacher("bob");
        Student student = Fixtures.student("eve");
        users.save(teacher); users.save(student);
        Result r = new ComplainAboutStudent(new SendMessage(messages, users, new IdSequence(), newLogger()), users, newLogger())
                .execute(teacher, student.username(), "lazy", UrgencyLevel.LOW);
        Assert.isFalse(r.success(), "Should fail with no Dean");
    }

    private static void testCreateOrg() {
        OrganizationRepository orgs = new InMemoryOrganizationRepository();
        Result r = new CreateOrganization(orgs, newLogger()).execute(Fixtures.student("eve"), "ChessClub");
        Assert.isTrue(r.success(), "Should create");
        Assert.isTrue(orgs.findByName("ChessClub").isPresent(), "Org persisted");
    }

    private static void testJoinOrg() {
        OrganizationRepository orgs = new InMemoryOrganizationRepository();
        new CreateOrganization(orgs, newLogger()).execute(Fixtures.student("head"), "Robotics");
        Result r = new JoinOrganization(orgs, newLogger()).execute(Fixtures.student("eve"), "Robotics");
        Assert.isTrue(r.success(), "Should join");
        Assert.isTrue(orgs.findByName("Robotics").orElseThrow().isMember(new Username("eve")), "Member added");
    }

    private static void testJoinOrgTwice() {
        OrganizationRepository orgs = new InMemoryOrganizationRepository();
        new CreateOrganization(orgs, newLogger()).execute(Fixtures.student("eve"), "Photo");
        Result r = new JoinOrganization(orgs, newLogger()).execute(Fixtures.student("eve"), "Photo");
        Assert.isFalse(r.success(), "Should fail re-joining");
    }

    private static void testSupervisorLowH() {
        UserRepository users = new InMemoryUserRepository();
        ResearchPaperRepository papers = new InMemoryResearchPaperRepository();
        Teacher mentor = activeTeacher("mentor", 0);
        users.save(mentor);
        GraduateStudent grad = new GraduateStudent(new Username("g"), "p", new PersonName("G", "G"),
                Gender.MALE, LocalDate.of(2000, 1, 1), new Email("g@g.com"), Faculty.SITE,
                DegreeType.MASTER, 1);
        Result r = new SetSupervisor(users, papers, new HIndexCalculator(), newLogger())
                .execute(grad, mentor.username());
        Assert.isFalse(r.success(), "Should reject low h-index");
    }

    private static void testSupervisorHighH() {
        UserRepository users = new InMemoryUserRepository();
        ResearchPaperRepository papers = new InMemoryResearchPaperRepository();
        Teacher mentor = activeTeacher("mentor", 5);
        users.save(mentor);
        for (int i = 1; i <= 3; i++) {
            ResearchPaper p = new ResearchPaper(new PaperId(i), "P" + i, mentor.username(),
                    new JournalName("J"), "abs", 5, null);
            for (int c = 0; c < 3; c++) p.cite();
            papers.save(p);
        }
        GraduateStudent grad = new GraduateStudent(new Username("g"), "p", new PersonName("G", "G"),
                Gender.MALE, LocalDate.of(2000, 1, 1), new Email("g@g.com"), Faculty.SITE,
                DegreeType.MASTER, 1);
        Result r = new SetSupervisor(users, papers, new HIndexCalculator(), newLogger())
                .execute(grad, mentor.username());
        Assert.isTrue(r.success(), "Should allow h-index >= 3");
    }

    private static void testComment() {
        NewsRepository news = new InMemoryNewsRepository();
        new PublishNews(news, new IdSequence(), newLogger()).execute(new Username("admin"), "T", "B");
        var n = news.findAllSorted().get(0);
        Result r = new CommentOnNews(news, newLogger()).execute(Fixtures.student("eve"), n.id(), "neat");
        Assert.isTrue(r.success(), "Comment should succeed");
        Assert.equals(1, news.findById(n.id()).orElseThrow().comments().size(), "Comment stored");
    }

    private static void testJoinProject() {
        ResearchProjectRepository projects = new InMemoryResearchProjectRepository();
        ResearchProject p = new ResearchProject(1, new JournalName("AI"), "topic", new Username("super"));
        projects.save(p);
        Student s = Fixtures.student("eve");
        new BecomeResearcher(newLogger()).execute(s, "AI");
        Result r = new JoinResearchProject(projects, newLogger()).execute(s, "AI");
        Assert.isTrue(r.success(), "Should join");
        Assert.isTrue(projects.findByJournal(new JournalName("AI")).orElseThrow()
                .participants().contains(s.username()), "Participant recorded");
    }
}
