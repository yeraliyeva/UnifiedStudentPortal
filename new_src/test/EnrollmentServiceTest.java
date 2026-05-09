package test;

import domain.course.Course;
import domain.repository.CourseRepository;
import domain.rules.AlreadyEnrolledRule;
import domain.rules.CapacityRule;
import domain.rules.CreditLimitRule;
import domain.rules.EnrollmentDecision;
import domain.rules.MaxFailLimitRule;
import domain.rules.PrerequisiteRule;
import domain.rules.ScheduleConflictRule;
import domain.service.EnrollmentService;
import domain.user.Student;
import infrastructure.persistence.inmemory.InMemoryCourseRepository;

import java.util.List;

public final class EnrollmentServiceTest {

    public static void runAll() {
        TestRunner.run("Service: full chain allows valid enrollment", EnrollmentServiceTest::testHappyPath);
        TestRunner.run("Service: chain short-circuits on first failure", EnrollmentServiceTest::testShortCircuit);
    }

    private static EnrollmentService service(CourseRepository repo) {
        return new EnrollmentService(List.of(
                new MaxFailLimitRule(),
                new AlreadyEnrolledRule(),
                new CreditLimitRule(),
                new PrerequisiteRule(repo),
                new CapacityRule(),
                new ScheduleConflictRule(repo)));
    }

    private static void testHappyPath() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course c = Fixtures.course("OK", 3, 30, 1);
        repo.save(c);
        EnrollmentDecision d = service(repo).tryEnroll(Fixtures.student("s1"), c);
        Assert.isTrue(d.allowed(), "Valid enrollment should be allowed");
    }

    private static void testShortCircuit() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course c = Fixtures.course("X", 3, 30, 1);
        repo.save(c);
        Student s = Fixtures.student("s1");
        s.recordFail(); s.recordFail(); s.recordFail();
        EnrollmentDecision d = service(repo).tryEnroll(s, c);
        Assert.isFalse(d.allowed(), "Should fail at MaxFailLimitRule");
        Assert.isTrue(d.reason().contains("fail limit"), "Reason should be the fail-limit one");
    }
}
