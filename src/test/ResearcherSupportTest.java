package test;

import domain.course.Course;
import domain.course.Grade;
import domain.enums.PaperFormat;
import domain.repository.CourseRepository;
import domain.research.HIndex;
import domain.research.JournalName;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.service.CitationFormatter;
import domain.service.GpaCalculator;
import domain.service.HIndexCalculator;
import domain.shared.Username;
import domain.user.Student;
import infrastructure.persistence.inmemory.InMemoryCourseRepository;

import java.util.List;

public final class ResearcherSupportTest {

    public static void runAll() {
        TestRunner.run("HIndex: 3 papers cited [3,2,1] -> 2", ResearcherSupportTest::testHIndex);
        TestRunner.run("GPA: average across enrolled graded courses", ResearcherSupportTest::testGpa);
        TestRunner.run("Citation: plain contains key fields", ResearcherSupportTest::testPlainCitation);
        TestRunner.run("Citation: bibtex format wraps in @article", ResearcherSupportTest::testBibtexCitation);
        TestRunner.run("Grade: letter A/B/C/D/F boundaries", ResearcherSupportTest::testGradeLetter);
    }

    private static void testHIndex() {
        ResearchPaper p1 = new ResearchPaper(new PaperId(1), "P1", new Username("a"), new JournalName("J1"), "x", 5, null);
        ResearchPaper p2 = new ResearchPaper(new PaperId(2), "P2", new Username("a"), new JournalName("J2"), "x", 5, null);
        ResearchPaper p3 = new ResearchPaper(new PaperId(3), "P3", new Username("a"), new JournalName("J3"), "x", 5, null);
        p1.cite();
        p2.cite(); p2.cite();
        p3.cite(); p3.cite(); p3.cite();
        HIndex h = new HIndexCalculator().calculate(List.of(p1, p2, p3));
        Assert.equals(2, h.value(), "h-index should be 2");
    }

    private static void testGpa() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course math = Fixtures.course("Math", 5, 30, 1);
        Course phys = Fixtures.course("Physics", 5, 30, 2);
        Student s = Fixtures.student("eve");
        s.recordEnrollment(math.id(), math.credits());
        s.recordEnrollment(phys.id(), phys.credits());
        math.enroll(s.username()); phys.enroll(s.username());
        math.recordGrade(s.username(), new Grade(30, 30, 40));
        phys.recordGrade(s.username(), new Grade(20, 20, 40));
        repo.save(math); repo.save(phys);
        double gpa = new GpaCalculator(repo).of(s);
        Assert.equals(90.0, gpa, "Average of 100 and 80 should be 90");
    }

    private static void testPlainCitation() {
        ResearchPaper p = new ResearchPaper(new PaperId(1), "Quantum OOP", new Username("alice"),
                new JournalName("Nature"), "abs", 12, "10.1/x");
        String c = new CitationFormatter().format(p, PaperFormat.PLAIN_TEXT);
        Assert.isTrue(c.contains("Quantum OOP"), "Should contain title");
        Assert.isTrue(c.contains("alice"), "Should contain author");
        Assert.isTrue(c.contains("Nature"), "Should contain journal");
        Assert.isTrue(c.contains("10.1/x"), "Should contain DOI");
    }

    private static void testBibtexCitation() {
        ResearchPaper p = new ResearchPaper(new PaperId(1), "Quantum OOP", new Username("alice"),
                new JournalName("Nature"), "abs", 12, "10.1/x");
        String c = new CitationFormatter().format(p, PaperFormat.BIBTEX);
        Assert.isTrue(c.startsWith("@article"), "Should start with @article");
        Assert.isTrue(c.contains("title   = {Quantum OOP}"), "Should have title field");
    }

    private static void testGradeLetter() {
        Assert.equals("A", new Grade(30, 30, 30).letter(), "90 -> A");
        Assert.equals("B", new Grade(30, 30, 20).letter(), "80 -> B");
        Assert.equals("C", new Grade(25, 25, 20).letter(), "70 -> C");
        Assert.equals("D", new Grade(20, 15, 15).letter(), "50 -> D");
        Assert.equals("F", new Grade(10, 10, 29).letter(), "49 -> F");
    }
}
