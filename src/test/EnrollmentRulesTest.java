package test;

import domain.course.Course;
import domain.course.Lesson;
import domain.course.Room;
import domain.course.TimeSlot;
import domain.enums.LessonType;
import domain.enums.WeekDay;
import domain.repository.CourseRepository;
import domain.rules.AlreadyEnrolledRule;
import domain.rules.CapacityRule;
import domain.rules.CreditLimitRule;
import domain.rules.EnrollmentDecision;
import domain.rules.MaxFailLimitRule;
import domain.rules.PrerequisiteRule;
import domain.rules.ScheduleConflictRule;
import domain.user.Student;
import infrastructure.persistence.inmemory.InMemoryCourseRepository;

public final class EnrollmentRulesTest {

    public static void runAll() {
        TestRunner.run("Rule: max fail limit blocks after 3 fails", EnrollmentRulesTest::testMaxFails);
        TestRunner.run("Rule: already-enrolled blocks duplicate", EnrollmentRulesTest::testAlreadyEnrolled);
        TestRunner.run("Rule: credit limit denies when not enough credits", EnrollmentRulesTest::testCreditLimit);
        TestRunner.run("Rule: prerequisite missing denies", EnrollmentRulesTest::testPrereqMissing);
        TestRunner.run("Rule: prerequisite satisfied allows", EnrollmentRulesTest::testPrereqSatisfied);
        TestRunner.run("Rule: capacity full denies", EnrollmentRulesTest::testCapacityFull);
        TestRunner.run("Rule: schedule conflict denies", EnrollmentRulesTest::testScheduleConflict);
    }

    private static void testMaxFails() {
        Student s = Fixtures.student("s1");
        s.recordFail(); s.recordFail(); s.recordFail();
        EnrollmentDecision d = new MaxFailLimitRule().check(s, Fixtures.course("X", 3, 30, 1));
        Assert.isFalse(d.allowed(), "Should deny after 3 fails");
    }

    private static void testAlreadyEnrolled() {
        Student s = Fixtures.student("s1");
        Course c = Fixtures.course("X", 3, 30, 1);
        s.recordEnrollment(c.id(), c.credits());
        EnrollmentDecision d = new AlreadyEnrolledRule().check(s, c);
        Assert.isFalse(d.allowed(), "Should deny duplicate enrollment");
    }

    private static void testCreditLimit() {
        Student s = Fixtures.student("s1");
        Course big = Fixtures.course("Big", 99, 30, 1);
        EnrollmentDecision d = new CreditLimitRule().check(s, big);
        Assert.isFalse(d.allowed(), "Should deny when credits exceed available");
    }

    private static void testPrereqMissing() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course math = Fixtures.course("Math", 5, 30, 1);
        Course oop = Fixtures.course("OOP", 5, 30, 2);
        oop.addPrerequisite(math.id());
        repo.save(math); repo.save(oop);
        Student s = Fixtures.student("s1");
        EnrollmentDecision d = new PrerequisiteRule(repo).check(s, oop);
        Assert.isFalse(d.allowed(), "Should deny when prereq not completed");
    }

    private static void testPrereqSatisfied() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course math = Fixtures.course("Math", 5, 30, 1);
        Course oop = Fixtures.course("OOP", 5, 30, 2);
        oop.addPrerequisite(math.id());
        repo.save(math); repo.save(oop);
        Student s = Fixtures.student("s1");
        s.recordCompletion(math.id());
        EnrollmentDecision d = new PrerequisiteRule(repo).check(s, oop);
        Assert.isTrue(d.allowed(), "Should allow when prereq completed");
    }

    private static void testCapacityFull() {
        Course tight = Fixtures.course("Tight", 3, 1, 1);
        tight.enroll(Fixtures.student("a").username());
        EnrollmentDecision d = new CapacityRule().check(Fixtures.student("b"), tight);
        Assert.isFalse(d.allowed(), "Should deny when capacity reached");
    }

    private static void testScheduleConflict() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course existing = Fixtures.course("A", 5, 30, 1);
        existing.addLesson(new Lesson(LessonType.LECTURE, new TimeSlot(WeekDay.MONDAY, "09:00"), new Room("R1")));
        Course incoming = Fixtures.course("B", 5, 30, 2);
        incoming.addLesson(new Lesson(LessonType.LECTURE, new TimeSlot(WeekDay.MONDAY, "09:00"), new Room("R2")));
        repo.save(existing); repo.save(incoming);
        Student s = Fixtures.student("s1");
        s.recordEnrollment(existing.id(), existing.credits());
        EnrollmentDecision d = new ScheduleConflictRule(repo).check(s, incoming);
        Assert.isFalse(d.allowed(), "Should deny on time slot collision");
    }
}
