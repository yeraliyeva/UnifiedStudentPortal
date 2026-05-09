package test;

import data.Database;
import education.Course;
import enums.DegreeType;
import enums.DisciplineType;
import enums.Faculty;
import enums.Gender;
import enums.ManagerPosition;
import enums.TeacherPosition;
import users.Admin;
import users.Manager;
import users.Teacher;

import java.time.LocalDate;

public class ManagerAdminTest {

    public static void runAll() {
        TestRunner.runTest("ManagerAdminTest: Admin User Creation", ManagerAdminTest::testAdminCreatesUsers);
        TestRunner.runTest("ManagerAdminTest: Manager Course Assignment", ManagerAdminTest::testManagerAssignsTeacher);
    }

    private static void testAdminCreatesUsers() {
        Database db = Database.getInstance();
        Admin admin = new Admin("Admin", "User", "admin1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE);
        
        admin.createStudent("S", "T", "stu1", "p", Gender.FEMALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.BACHELOR, 1);
        admin.createManager("M", "A", "mgr1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, 1000, LocalDate.now(), "INS", ManagerPosition.OR);
        
        Assert.assertTrue(db.userExists("stu1"), "Student should be in database");
        Assert.assertTrue(db.userExists("mgr1"), "Manager should be in database");
        Assert.assertTrue(db.getUser("mgr1") instanceof Manager, "mgr1 should be of type Manager");
    }

    private static void testManagerAssignsTeacher() {
        Course course = new Course("Software Engineering", 5, DisciplineType.MAJOR);
        Teacher teacher = new Teacher("T", "T", "tch1", "p", Gender.FEMALE, LocalDate.now(), "e", Faculty.SITE, 2000, LocalDate.now(), "INS", "PhD", TeacherPosition.PROFESSOR);
        
        teacher.assignToCourse(course);
        
        Assert.assertTrue(course.getTeachers().contains(teacher), "Teacher should be assigned to the course");
        Assert.assertTrue(teacher.getTaughtCourses().contains(course), "Course should be in teacher's list");
    }
}
