package test;

import application.Result;
import application.usecase.user.BecomeResearcher;
import domain.repository.LogRepository;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import infrastructure.logging.RepositoryLogger;
import infrastructure.persistence.inmemory.InMemoryLogRepository;

public final class BecomeResearcherTest {

    public static void runAll() {
        TestRunner.run("BecomeResearcher: student activates successfully", BecomeResearcherTest::testStudent);
        TestRunner.run("BecomeResearcher: teacher activates successfully", BecomeResearcherTest::testTeacher);
        TestRunner.run("BecomeResearcher: cannot activate twice", BecomeResearcherTest::testTwiceFails);
        TestRunner.run("BecomeResearcher: not-capable role rejected", BecomeResearcherTest::testNotCapable);
    }

    private static BecomeResearcher useCase() {
        LogRepository logs = new InMemoryLogRepository();
        return new BecomeResearcher(new RepositoryLogger(logs));
    }

    private static void testStudent() {
        Student s = Fixtures.student("s1");
        Assert.isFalse(s.isResearcher(), "Initially not a researcher");
        Result r = useCase().execute(s, "AI");
        Assert.isTrue(r.success(), "Activation should succeed");
        Assert.isTrue(s.isResearcher(), "Student should be researcher now");
        Assert.equals("AI", s.researcherProfile().field(), "Field stored correctly");
    }

    private static void testTeacher() {
        Teacher t = Fixtures.teacher("t1");
        Result r = useCase().execute(t, "Mathematics");
        Assert.isTrue(r.success(), "Activation should succeed for teacher");
        Assert.isTrue(t.isResearcher(), "Teacher should be researcher now");
    }

    private static void testTwiceFails() {
        Student s = Fixtures.student("s1");
        useCase().execute(s, "AI");
        Result r = useCase().execute(s, "Bio");
        Assert.isFalse(r.success(), "Second activation should fail");
    }

    private static void testNotCapable() {
        User admin = new domain.user.Admin(new domain.shared.Username("a"), "p",
                new domain.shared.PersonName("A", "B"), domain.enums.Gender.MALE,
                java.time.LocalDate.now(), new domain.shared.Email("a@a.com"),
                domain.enums.Faculty.SITE);
        Result r = useCase().execute(admin, "X");
        Assert.isFalse(r.success(), "Admin cannot become researcher");
    }
}
