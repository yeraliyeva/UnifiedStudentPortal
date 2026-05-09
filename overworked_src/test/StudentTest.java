package test;

import education.Course;
import enums.DegreeType;
import enums.DisciplineType;
import enums.Faculty;
import enums.Gender;
import users.Student;

import java.time.LocalDate;

public class StudentTest {

    public static void runAll() {
        TestRunner.runTest("StudentTest: Enroll in Course (Success)", StudentTest::testEnrollCourseSuccess);
        TestRunner.runTest("StudentTest: Credit Limit Exceeded", StudentTest::testEnrollCourseCreditLimit);
        TestRunner.runTest("StudentTest: Max Fail Limit", StudentTest::testMaxFails);
        TestRunner.runTest("StudentTest: Calculate GPA", StudentTest::testCalculateGPA);
        TestRunner.runTest("StudentTest: Drop Course", StudentTest::testDropCourse);
    }

    private static void testEnrollCourseSuccess() {
        Student s = new Student("A", "B", "stu1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.BACHELOR, 1);
        Course c = new Course("OOP", 5, DisciplineType.MAJOR);
        s.enrollCourse(c);

        Assert.assertTrue(s.getEnrolledCourses().contains(c), "Student should be enrolled in course");
        Assert.assertEquals(16, s.getAvailableCredits(), "Available credits should decrease from 21 to 16");
    }

    private static void testEnrollCourseCreditLimit() {
        Student s = new Student("A", "B", "stu1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.BACHELOR, 1);
        Course c1 = new Course("Math", 10, DisciplineType.MAJOR);
        Course c2 = new Course("Physics", 10, DisciplineType.MAJOR);
        Course c3 = new Course("Chemistry", 5, DisciplineType.MAJOR);

        s.enrollCourse(c1); // 21 - 10 = 11
        s.enrollCourse(c2); // 11 - 10 = 1
        s.enrollCourse(c3); // Should fail (1 < 5)

        Assert.assertEquals(1, s.getAvailableCredits(), "Credits should be exactly 1");
        Assert.assertFalse(s.getEnrolledCourses().contains(c3), "Student should NOT be enrolled in Chemistry due to lack of credits");
    }

    private static void testMaxFails() {
        Student s = new Student("A", "B", "stu1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.BACHELOR, 1);
        s.recordFail();
        s.recordFail();
        s.recordFail(); // 3rd fail
        
        Course c = new Course("History", 3, DisciplineType.MINOR);
        s.enrollCourse(c); // Should be blocked

        Assert.assertFalse(s.getEnrolledCourses().contains(c), "Student should be blocked from enrolling due to 3 fails");
        Assert.assertEquals(3, s.getFailCount(), "Fail count should be 3");
    }

    private static void testCalculateGPA() {
        Student s = new Student("A", "B", "stu1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.BACHELOR, 1);
        Course c1 = new Course("Math", 5, DisciplineType.MAJOR);
        Course c2 = new Course("Physics", 5, DisciplineType.MAJOR);
        s.enrollCourse(c1);
        s.enrollCourse(c2);

        // Teacher puts marks manually for test
        c1.setGrade("stu1", 30, 30, 40); // 100
        c2.setGrade("stu1", 20, 20, 40); // 80

        double gpa = s.calculateGPA();
        Assert.assertEquals(90.0, gpa, "Average GPA of 100 and 80 should be 90.0");
    }

    private static void testDropCourse() {
        Student s = new Student("A", "B", "stu1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.BACHELOR, 1);
        Course c = new Course("Math", 5, DisciplineType.MAJOR);
        
        s.enrollCourse(c);
        Assert.assertEquals(16, s.getAvailableCredits(), "16 credits left");
        
        s.dropCourse(c);
        Assert.assertEquals(21, s.getAvailableCredits(), "Credits should be restored to 21");
        Assert.assertTrue(s.getEnrolledCourses().isEmpty(), "Enrolled courses should be empty");
    }
}
