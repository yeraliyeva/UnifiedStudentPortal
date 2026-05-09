package test;

import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import education.AttestationResult;
import education.Course;
import education.Lesson;
import enums.DegreeType;
import enums.DisciplineType;
import enums.Faculty;
import enums.Gender;
import enums.LessonType;
import enums.ManagerPosition;
import enums.PaperFormat;
import enums.WeekDay;
import users.Manager;
import users.Student;

import java.time.LocalDate;

public class AcademicExtrasTest {

    public static void runAll() {
        TestRunner.runTest("AcademicExtras: Letter grade A/B/C/D/F boundaries", AcademicExtrasTest::testLetterGradeBoundaries);
        TestRunner.runTest("AcademicExtras: AttestationResult.isPassing threshold", AcademicExtrasTest::testIsPassing);
        TestRunner.runTest("AcademicExtras: markCourseCompleted records course", AcademicExtrasTest::testMarkCompleted);
        TestRunner.runTest("AcademicExtras: Citation plain text contains key fields", AcademicExtrasTest::testCitationPlain);
        TestRunner.runTest("AcademicExtras: Citation BibTeX format", AcademicExtrasTest::testCitationBibtex);
        TestRunner.runTest("AcademicExtras: Journal subscribe -> publish notifies subscriber", AcademicExtrasTest::testSubscriptionNotification);
        TestRunner.runTest("AcademicExtras: Unsubscribe stops notifications", AcademicExtrasTest::testUnsubscribeStops);
        TestRunner.runTest("AcademicExtras: LessonType.EXAM exists and round-trips", AcademicExtrasTest::testExamLessonType);
        TestRunner.runTest("AcademicExtras: Generate academic report runs without error", AcademicExtrasTest::testGenerateReport);
    }

    private static Student newStudent(String username) {
        return new Student("F", "L", username, "p", Gender.MALE, LocalDate.now(),
                "e", Faculty.SITE, DegreeType.BACHELOR, 1);
    }

    private static void testLetterGradeBoundaries() {
        Assert.assertEquals("A", new AttestationResult(30, 30, 30).getLetterGrade(), "90 -> A");
        Assert.assertEquals("A", new AttestationResult(30, 30, 40).getLetterGrade(), "100 -> A");
        Assert.assertEquals("B", new AttestationResult(30, 30, 20).getLetterGrade(), "80 -> B");
        Assert.assertEquals("B", new AttestationResult(30, 29, 30).getLetterGrade(), "89 -> B");
        Assert.assertEquals("C", new AttestationResult(25, 25, 20).getLetterGrade(), "70 -> C");
        Assert.assertEquals("D", new AttestationResult(20, 15, 15).getLetterGrade(), "50 -> D");
        Assert.assertEquals("F", new AttestationResult(10, 10, 29).getLetterGrade(), "49 -> F");
        Assert.assertEquals("F", new AttestationResult(0, 0, 0).getLetterGrade(), "0 -> F");
    }

    private static void testIsPassing() {
        Assert.assertTrue(new AttestationResult(20, 15, 15).isPassing(), "50 should pass");
        Assert.assertFalse(new AttestationResult(20, 15, 14).isPassing(), "49 should not pass");
    }

    private static void testMarkCompleted() {
        Student s = newStudent("stu1");
        Course math = new Course("Math", 5, DisciplineType.MAJOR);

        s.markCourseCompleted(math);
        s.markCourseCompleted(math);

        Assert.assertEquals(1, s.getCompletedCourses().size(),
                "Duplicate completion should be ignored");
        Assert.assertTrue(s.getCompletedCourses().contains(math), "Math should be in completed list");
    }

    private static void testCitationPlain() {
        ResearchPaper p = new ResearchPaper("Quantum OOP", "alice", "Nature", "abstract", 12, "10.1/x");
        String c = p.getCitation(PaperFormat.PLAIN_TEXT);

        Assert.assertTrue(c.contains("Quantum OOP"), "Plain citation should include title");
        Assert.assertTrue(c.contains("alice"), "Plain citation should include author");
        Assert.assertTrue(c.contains("Nature"), "Plain citation should include journal");
        Assert.assertTrue(c.contains("10.1/x"), "Plain citation should include DOI when set");
    }

    private static void testCitationBibtex() {
        ResearchPaper p = new ResearchPaper("Quantum OOP", "alice", "Nature", "abstract", 12, "10.1/x");
        String c = p.getCitation(PaperFormat.BIBTEX);

        Assert.assertTrue(c.startsWith("@article"), "BibTeX should start with @article");
        Assert.assertTrue(c.contains("title   = {Quantum OOP}"), "BibTeX should include title field");
        Assert.assertTrue(c.contains("journal = {Nature}"), "BibTeX should include journal field");
        Assert.assertTrue(c.contains("doi     = {10.1/x}"), "BibTeX should include DOI when set");
    }

    private static void testSubscriptionNotification() {
        Student s = newStudent("stu1");
        Database.getInstance().addUser(s);

        ResearchProject proj = new ResearchProject("AI Journal", "AI Topic", "supervisor");
        Database.getInstance().addResearchProject(proj);

        s.subscribeToJournal("AI Journal");
        Assert.assertTrue(s.getSubscribedJournals().contains("AI Journal"),
                "Student should track AI Journal in subscriptions");

        ResearchPaper paper = new ResearchPaper("New AI Paper", "auth", "AI Journal", "abs", 5, null);
        proj.publishPaper(paper);

        Assert.assertFalse(s.getNotifications().isEmpty(),
                "Subscribed student should have received a notification");
        Assert.assertTrue(s.getNotifications().get(0).contains("New AI Paper"),
                "Notification should mention the new paper title");
    }

    private static void testUnsubscribeStops() {
        Student s = newStudent("stu1");
        Database.getInstance().addUser(s);

        ResearchProject proj = new ResearchProject("Bio Journal", "Bio", "sup");
        Database.getInstance().addResearchProject(proj);

        s.subscribeToJournal("Bio Journal");
        s.unsubscribeFromJournal("Bio Journal");

        ResearchPaper paper = new ResearchPaper("Cells 101", "auth", "Bio Journal", "abs", 5, null);
        proj.publishPaper(paper);

        Assert.assertTrue(s.getNotifications().isEmpty(),
                "Unsubscribed student should NOT receive a notification");
        Assert.assertFalse(s.getSubscribedJournals().contains("Bio Journal"),
                "Subscription should be removed from local set");
    }

    private static void testExamLessonType() {
        Course c = new Course("Algorithms", 5, DisciplineType.MAJOR);
        Lesson exam = new Lesson(LessonType.EXAM, WeekDay.FRIDAY, "14:00", "Hall-A");
        c.addLesson(exam);

        Assert.assertEquals(LessonType.EXAM, c.getLessons().get(0).getType(), "Lesson type should be EXAM");
        long examCount = c.getLessons().stream().filter(l -> l.getType() == LessonType.EXAM).count();
        Assert.assertEquals(1L, examCount, "Should find one EXAM lesson on the course");
    }

    private static void testGenerateReport() {
        Manager m = new Manager("M", "A", "mgr1", "p", Gender.MALE, LocalDate.now(),
                "e", Faculty.SITE, 1000, LocalDate.now(), "INS", ManagerPosition.OR);
        Database.getInstance().addUser(m);

        Course c1 = new Course("Math", 5, DisciplineType.MAJOR, 50);
        Course c2 = new Course("Physics", 5, DisciplineType.MAJOR, 50);
        Database.getInstance().addCourse(c1);
        Database.getInstance().addCourse(c2);

        Student s1 = newStudent("s1");
        Student s2 = newStudent("s2");
        Database.getInstance().addUser(s1);
        Database.getInstance().addUser(s2);

        s1.enrollCourse(c1);
        s2.enrollCourse(c1);
        c1.setGrade("s1", 30, 30, 30);
        c1.setGrade("s2", 20, 15, 14);

        m.generateAcademicReport();

        Assert.assertTrue(true, "generateAcademicReport completed without throwing");
    }
}
