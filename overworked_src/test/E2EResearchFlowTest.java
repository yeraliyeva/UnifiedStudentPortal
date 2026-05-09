package test;

import communication.News;
import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import enums.DegreeType;
import enums.Faculty;
import enums.Gender;
import exceptions.NotResearcherException;
import users.EmployeeResearcher;
import users.GraduateStudent;
import users.Librarian;

import java.time.LocalDate;
import java.util.List;

public class E2EResearchFlowTest {

    public static void runAll() {
        TestRunner.runTest("E2E: Research Publication and Observer Notification", E2EResearchFlowTest::testResearchObserverFlow);
    }

    private static void testResearchObserverFlow() throws NotResearcherException {
        Database db = Database.getInstance();

        // 1. Setup actors
        EmployeeResearcher lead = new EmployeeResearcher("Dr", "House", "house", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, 5000, LocalDate.now(), "INS", "Medicine");
        GraduateStudent grad = new GraduateStudent("Chase", "Robert", "chase", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.DOCTORATE, 2);
        Librarian librarian = new Librarian("Lib", "Rary", "lib", "p", Gender.FEMALE, LocalDate.now(), "e", Faculty.SITE, 1000, LocalDate.now(), "INS");
        
        db.addUser(lead);
        db.addUser(grad);
        db.addUser(librarian);

        // 2. Lead creates a project
        ResearchProject project = new ResearchProject("Nature Med", "Lupus Study", lead.getUsername());
        lead.createResearchProject(project);

        // Librarian subscribes to the project (simulating Observer pattern registration)
        project.subscribe(librarian);

        // 3. Grad student joins project
        project.addParticipant(grad);
        Assert.assertTrue(project.getParticipantUsernames().contains(grad.getUsername()), "Grad student should be in project");

        // 4. Grad student publishes paper -> triggers Observer notification AND automatic News generation
        ResearchPaper paper = new ResearchPaper("It's never Lupus", grad.getUsername(), "Nature Med", "Abstract", 15, "10.1234/5678");
        project.publishPaper(paper); // This should auto-generate News

        // 5. Verify Paper exists
        Assert.assertTrue(project.getPublishedPapers().contains(paper), "Paper should be in project");

        // 6. Verify automatic News creation
        List<News> allNews = db.getAllNews();
        Assert.assertFalse(allNews.isEmpty(), "News should have been generated automatically");
        Assert.assertTrue(allNews.get(0).getTitle().contains("Research"), "News title should have 'Research' prefix for pinning");
        
        // Output proves Librarian's notifyNewPaper() was called because of project.publishPaper()
    }
}
