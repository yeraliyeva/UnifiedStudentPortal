package test;

import common.PaperComparators;
import communication.ResearchPaper;
import communication.ResearchProject;
import enums.DegreeType;
import enums.Faculty;
import enums.Gender;
import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import users.EmployeeResearcher;
import users.GraduateStudent;
import users.ResearcherDecorator;
import users.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResearchTest {

    public static void runAll() {
        TestRunner.runTest("ResearchTest: H-Index Calculation", ResearchTest::testHIndex);
        TestRunner.runTest("ResearchTest: ResearcherDecorator", ResearchTest::testDecorator);
        TestRunner.runTest("ResearchTest: Grad Student Validation", ResearchTest::testGradStudentValidation);
        TestRunner.runTest("ResearchTest: Paper Sort Strategies", ResearchTest::testPaperSorting);
        TestRunner.runTest("ResearchTest: Supervisor Assignment", ResearchTest::testSupervisorLowHIndex);
    }

    private static void testHIndex() {
        EmployeeResearcher r = new EmployeeResearcher("A", "B", "res1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, 1000, LocalDate.now(), "INS", "AI");
        
        ResearchPaper p1 = new ResearchPaper("P1", "res1", "J1", "abc", 10, null); p1.incrementCitations();
        ResearchPaper p2 = new ResearchPaper("P2", "res1", "J2", "abc", 10, null); p2.incrementCitations(); p2.incrementCitations();
        ResearchPaper p3 = new ResearchPaper("P3", "res1", "J3", "abc", 10, null); p3.incrementCitations(); p3.incrementCitations(); p3.incrementCitations();
        
        // Citations: p3=3, p2=2, p1=1 -> h-index should be 2.
        // Because:
        // 1st paper (p3): 3 >= 1 (ok)
        // 2nd paper (p2): 2 >= 2 (ok)
        // 3rd paper (p1): 1 >= 3 (fail) -> stop at 2.
        
        r.addResearchPaper(p1);
        r.addResearchPaper(p2);
        r.addResearchPaper(p3);

        int hIndex = r.calculateHIndex();
        Assert.assertEquals(2, hIndex, "H-Index should be 2 based on citations [3, 2, 1]");
    }

    private static void testDecorator() throws NotResearcherException {
        // Standard student is NOT a researcher
        Student s = new Student("A", "B", "stu1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.BACHELOR, 1);
        
        // Decorate student
        ResearcherDecorator decorated = new ResearcherDecorator(s);
        
        ResearchPaper p = new ResearchPaper("DecPaper", "stu1", "J", "a", 5, null);
        decorated.addResearchPaper(p);

        Assert.assertEquals(1, decorated.getMyPapers().size(), "Decorated user should be able to add papers");
        Assert.assertEquals(0, decorated.calculateHIndex(), "Initial H-index should be 0");
    }

    private static void testGradStudentValidation() {
        try {
            // Should throw exception because DegreeType is BACHELOR
            new GraduateStudent("A", "B", "g1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.BACHELOR, 1);
            Assert.assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true, "Exception successfully thrown for invalid GradStudent degree");
        }
    }

    private static void testPaperSorting() {
        ResearchPaper p1 = new ResearchPaper("A Title", "u1", "J1", "a", 20, null); // 20 pages, 0 cites, date now
        ResearchPaper p2 = new ResearchPaper("Z Title", "u1", "J2", "a", 10, null); // 10 pages, 2 cites
        p2.incrementCitations(); p2.incrementCitations();
        ResearchPaper p3 = new ResearchPaper("M Title", "u1", "J3", "a", 30, null); // 30 pages, 1 cite
        p3.incrementCitations();

        List<ResearchPaper> list = new ArrayList<>();
        list.add(p1); list.add(p2); list.add(p3);

        // Sort by Citations (Descending)
        Collections.sort(list, PaperComparators.BY_CITATIONS);
        Assert.assertEquals("Z Title", list.get(0).getTitle(), "Highest citation should be first");

        // Sort by Length (Descending)
        Collections.sort(list, PaperComparators.BY_LENGTH);
        Assert.assertEquals("M Title", list.get(0).getTitle(), "Longest paper should be first");

        // Sort by Title (Ascending)
        Collections.sort(list, PaperComparators.BY_TITLE);
        Assert.assertEquals("A Title", list.get(0).getTitle(), "Alphabetically first title should be first");
    }

    private static void testSupervisorLowHIndex() {
        GraduateStudent gs = new GraduateStudent("A", "B", "g1", "p", Gender.MALE, LocalDate.now(), "e@e.com", Faculty.SITE, DegreeType.MASTER, 1);
        EmployeeResearcher weakSupervisor = new EmployeeResearcher("W", "S", "ws1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, 100, LocalDate.now(), "INS", "AI");
        
        try {
            gs.setSupervisor(weakSupervisor);
            Assert.assertTrue(false, "Should have thrown LowHIndexException");
        } catch (LowHIndexException e) {
            Assert.assertTrue(true, "LowHIndexException caught successfully for H-index 0");
        }
    }
}
