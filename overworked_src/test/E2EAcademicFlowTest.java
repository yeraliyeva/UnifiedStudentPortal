package test;

import communication.Message;
import data.Database;
import education.Course;
import enums.DegreeType;
import enums.DisciplineType;
import enums.Faculty;
import enums.Gender;
import enums.ManagerPosition;
import enums.TeacherPosition;
import users.Admin;
import users.Dean;
import users.Manager;
import users.Student;
import users.Teacher;

import java.time.LocalDate;
import java.util.List;

public class E2EAcademicFlowTest {

    public static void runAll() {
        TestRunner.runTest("E2E: Full Academic Lifecycle", E2EAcademicFlowTest::testFullLifecycle);
    }

    private static void testFullLifecycle() {
        Database db = Database.getInstance();

        // 1. Admin creates roles
        Admin admin = new Admin("System", "Admin", "admin1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE);
        Student student = admin.createStudent("Eve", "Smith", "eve", "p", Gender.FEMALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.BACHELOR, 1);
        Teacher teacher = admin.createTeacher("Bob", "Ross", "bob", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, 2000, LocalDate.now(), "INS", "PhD", true);
        Manager manager = admin.createManager("Dave", "Johnson", "dave", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, 1500, LocalDate.now(), "INS", ManagerPosition.OR);
        Dean dean = admin.createDean("Carol", "White", "carol", "p", Gender.FEMALE, LocalDate.now(), "e", Faculty.SITE, 3000, LocalDate.now(), "INS", "PhD");

        // 2. Manager creates Course and assigns Teacher
        Course course = new Course("OOP", 5, DisciplineType.MAJOR);
        db.addCourse(course);
        teacher.assignToCourse(course);

        // 3. Student enrolls
        student.enrollCourse(course);
        Assert.assertTrue(course.getStudents().contains(student), "Student should be in course");

        // 4. Teacher puts failing marks (total < 50)
        course.setGrade("eve", 10, 10, 20); // Total 40
        int failCountBefore = student.getFailCount();
        
        // Simulating the failure logic from Teacher.putMarksInteractive
        if (course.getGrade("eve").getTotal() < 50) {
            student.recordFail();
        }

        Assert.assertEquals(failCountBefore + 1, student.getFailCount(), "Student should receive 1 fail");

        // 5. Student formally complains to Dean about Teacher
        teacher.sendComplaintAboutStudent("eve", "Student is failing constantly", enums.UrgencyLevel.HIGH);

        // 6. Dean receives message in Inbox
        List<Message> deanInbox = db.getMessagesFor("carol");
        Assert.assertFalse(deanInbox.isEmpty(), "Dean should have received a message");
        Assert.assertEquals("Complaint about student: eve", deanInbox.get(0).getSubject(), "Subject should match complaint");
        Assert.assertEquals("bob", deanInbox.get(0).getSender(), "Sender should be the teacher");
    }
}
