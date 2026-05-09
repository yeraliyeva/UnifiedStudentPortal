package test;

import education.Course;
import education.Lesson;
import enums.DegreeType;
import enums.DisciplineType;
import enums.Faculty;
import enums.Gender;
import enums.LessonType;
import enums.ManagerPosition;
import enums.WeekDay;
import users.Manager;
import users.Student;

import java.time.LocalDate;

public class EnrollmentChecksTest {

    public static void runAll() {
        TestRunner.runTest("EnrollmentChecks: Prerequisite missing blocks enroll", EnrollmentChecksTest::testPrereqBlocks);
        TestRunner.runTest("EnrollmentChecks: Prerequisite satisfied allows enroll", EnrollmentChecksTest::testPrereqSatisfied);
        TestRunner.runTest("EnrollmentChecks: Capacity full blocks enroll", EnrollmentChecksTest::testCapacityBlocks);
        TestRunner.runTest("EnrollmentChecks: Capacity not full allows enroll", EnrollmentChecksTest::testCapacityAllows);
        TestRunner.runTest("EnrollmentChecks: Schedule conflict blocks enroll", EnrollmentChecksTest::testScheduleConflictBlocks);
        TestRunner.runTest("EnrollmentChecks: Different time slot allows enroll", EnrollmentChecksTest::testNoConflictAllows);
        TestRunner.runTest("EnrollmentChecks: Room collision detected by Manager", EnrollmentChecksTest::testRoomCollision);
        TestRunner.runTest("EnrollmentChecks: Room free when day or time differs", EnrollmentChecksTest::testRoomFree);
    }

    private static Student newStudent() {
        return new Student("A", "B", "stu1", "p", Gender.MALE, LocalDate.now(),
                "e", Faculty.SITE, DegreeType.BACHELOR, 1);
    }

    private static Manager newManager() {
        return new Manager("M", "A", "mgr1", "p", Gender.MALE, LocalDate.now(),
                "e", Faculty.SITE, 1000, LocalDate.now(), "INS", ManagerPosition.OR);
    }

    private static void testPrereqBlocks() {
        Student s = newStudent();
        Course math = new Course("Math", 5, DisciplineType.MAJOR);
        Course oop = new Course("OOP", 5, DisciplineType.MAJOR);
        oop.addPrerequisite(math);

        s.enrollCourse(oop);

        Assert.assertFalse(s.getEnrolledCourses().contains(oop),
                "Student should NOT be enrolled in OOP without completing Math");
        Assert.assertEquals(21, s.getAvailableCredits(),
                "Credits should not be deducted when enrollment is rejected");
    }

    private static void testPrereqSatisfied() {
        Student s = newStudent();
        Course math = new Course("Math", 5, DisciplineType.MAJOR);
        Course oop = new Course("OOP", 5, DisciplineType.MAJOR);
        oop.addPrerequisite(math);

        s.markCourseCompleted(math);
        s.enrollCourse(oop);

        Assert.assertTrue(s.getEnrolledCourses().contains(oop),
                "Student SHOULD be enrolled in OOP after completing Math");
        Assert.assertEquals(16, s.getAvailableCredits(), "Credits deducted normally");
    }

    private static void testCapacityBlocks() {
        Course tight = new Course("Tight", 3, DisciplineType.MAJOR, 2);

        Student a = new Student("A", "X", "a1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.BACHELOR, 1);
        Student b = new Student("B", "Y", "b1", "p", Gender.FEMALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.BACHELOR, 1);
        Student c = new Student("C", "Z", "c1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.BACHELOR, 1);

        a.enrollCourse(tight);
        b.enrollCourse(tight);
        c.enrollCourse(tight);

        Assert.assertEquals(2, tight.getStudents().size(),
                "Course capacity 2 should hold exactly 2 students");
        Assert.assertTrue(tight.isFull(), "Course should be full");
        Assert.assertFalse(c.getEnrolledCourses().contains(tight),
                "Third student should be blocked");
    }

    private static void testCapacityAllows() {
        Course room = new Course("Room", 3, DisciplineType.MAJOR, 5);
        Student s = newStudent();
        s.enrollCourse(room);

        Assert.assertTrue(s.getEnrolledCourses().contains(room),
                "Student should be enrolled while seats remain");
        Assert.assertEquals(4, room.getRemainingSeats(),
                "Remaining seats should drop to 4 after enrollment");
    }

    private static void testScheduleConflictBlocks() {
        Student s = newStudent();
        Course a = new Course("A", 5, DisciplineType.MAJOR);
        Course b = new Course("B", 5, DisciplineType.MAJOR);

        a.addLesson(new Lesson(LessonType.LECTURE, WeekDay.MONDAY, "09:00", "R1"));
        b.addLesson(new Lesson(LessonType.LECTURE, WeekDay.MONDAY, "09:00", "R2"));

        s.enrollCourse(a);
        s.enrollCourse(b);

        Assert.assertTrue(s.getEnrolledCourses().contains(a), "Course A should be enrolled");
        Assert.assertFalse(s.getEnrolledCourses().contains(b),
                "Course B with same Monday 09:00 slot should be blocked");
    }

    private static void testNoConflictAllows() {
        Student s = newStudent();
        Course a = new Course("A", 5, DisciplineType.MAJOR);
        Course b = new Course("B", 5, DisciplineType.MAJOR);

        a.addLesson(new Lesson(LessonType.LECTURE, WeekDay.MONDAY, "09:00", "R1"));
        b.addLesson(new Lesson(LessonType.LECTURE, WeekDay.MONDAY, "10:30", "R1"));

        s.enrollCourse(a);
        s.enrollCourse(b);

        Assert.assertTrue(s.getEnrolledCourses().contains(a), "Course A enrolled");
        Assert.assertTrue(s.getEnrolledCourses().contains(b),
                "Course B at different time should be allowed");
    }

    private static void testRoomCollision() {
        Manager m = newManager();
        Course a = new Course("Aero", 5, DisciplineType.MAJOR);
        a.addLesson(new Lesson(LessonType.LECTURE, WeekDay.TUESDAY, "11:00", "Room-101"));
        data.Database.getInstance().addCourse(a);

        Assert.assertFalse(m.isRoomAvailable("Room-101", WeekDay.TUESDAY, "11:00"),
                "Room should be detected as booked");
        Assert.assertFalse(m.isRoomAvailable("room-101", WeekDay.TUESDAY, "11:00"),
                "Room comparison should be case-insensitive");
    }

    private static void testRoomFree() {
        Manager m = newManager();
        Course a = new Course("Bio", 5, DisciplineType.MAJOR);
        a.addLesson(new Lesson(LessonType.LECTURE, WeekDay.TUESDAY, "11:00", "Room-101"));
        data.Database.getInstance().addCourse(a);

        Assert.assertTrue(m.isRoomAvailable("Room-101", WeekDay.WEDNESDAY, "11:00"),
                "Different day should be free");
        Assert.assertTrue(m.isRoomAvailable("Room-101", WeekDay.TUESDAY, "13:00"),
                "Different time should be free");
        Assert.assertTrue(m.isRoomAvailable("Room-202", WeekDay.TUESDAY, "11:00"),
                "Different room should be free");
    }
}
