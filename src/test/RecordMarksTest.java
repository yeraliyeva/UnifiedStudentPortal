package test;

import application.Result;
import application.usecase.course.RecordMarks;
import domain.course.Course;
import domain.course.Grade;
import domain.repository.CourseRepository;
import domain.repository.UserRepository;
import domain.user.Student;
import domain.user.Teacher;
import infrastructure.logging.RepositoryLogger;
import infrastructure.persistence.inmemory.InMemoryCourseRepository;
import infrastructure.persistence.inmemory.InMemoryLogRepository;
import infrastructure.persistence.inmemory.InMemoryUserRepository;

public final class RecordMarksTest {

    public static void runAll() {
        TestRunner.run("RecordMarks: passing grade marks course completed", RecordMarksTest::testPassMarksCompleted);
        TestRunner.run("RecordMarks: failing grade increments fail count", RecordMarksTest::testFailIncrements);
        TestRunner.run("RecordMarks: teacher must own course", RecordMarksTest::testTeacherOwnership);
    }

    private static RecordMarks newUseCase(CourseRepository courses, UserRepository users) {
        return new RecordMarks(courses, users, new RepositoryLogger(new InMemoryLogRepository()));
    }

    private static void testPassMarksCompleted() {
        UserRepository users = new InMemoryUserRepository();
        CourseRepository courses = new InMemoryCourseRepository();
        Student s = Fixtures.student("eve");
        Teacher t = Fixtures.teacher("bob");
        Course c = Fixtures.course("Math", 5, 30, 1);
        c.assignTeacher(t.username());
        s.recordEnrollment(c.id(), c.credits());
        c.enroll(s.username());
        users.save(s); users.save(t); courses.save(c);

        Result r = newUseCase(courses, users).execute(t, c.id(), s.username(), new Grade(30, 30, 30));
        Assert.isTrue(r.success(), "Should succeed");
        Assert.isTrue(s.completedCourses().contains(c.id()), "Course should be marked completed");
        Assert.equals(0, s.failCount(), "Pass should not increment fails");
    }

    private static void testFailIncrements() {
        UserRepository users = new InMemoryUserRepository();
        CourseRepository courses = new InMemoryCourseRepository();
        Student s = Fixtures.student("eve");
        Teacher t = Fixtures.teacher("bob");
        Course c = Fixtures.course("Math", 5, 30, 1);
        c.assignTeacher(t.username());
        s.recordEnrollment(c.id(), c.credits());
        c.enroll(s.username());
        users.save(s); users.save(t); courses.save(c);

        newUseCase(courses, users).execute(t, c.id(), s.username(), new Grade(10, 10, 20));
        Assert.equals(1, s.failCount(), "Fail count should be 1 after a failing grade");
        Assert.isFalse(s.completedCourses().contains(c.id()), "Failing grade does not mark completion");
    }

    private static void testTeacherOwnership() {
        UserRepository users = new InMemoryUserRepository();
        CourseRepository courses = new InMemoryCourseRepository();
        Student s = Fixtures.student("eve");
        Teacher mine = Fixtures.teacher("mine");
        Teacher other = Fixtures.teacher("other");
        Course c = Fixtures.course("Math", 5, 30, 1);
        c.assignTeacher(mine.username());
        s.recordEnrollment(c.id(), c.credits()); c.enroll(s.username());
        users.save(s); users.save(mine); users.save(other); courses.save(c);

        Result r = newUseCase(courses, users).execute(other, c.id(), s.username(), new Grade(30, 30, 30));
        Assert.isFalse(r.success(), "Non-owning teacher cannot record marks");
    }
}
