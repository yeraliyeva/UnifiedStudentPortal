package test;

import communication.News;
import communication.Organization;
import data.Database;
import enums.DegreeType;
import enums.Faculty;
import enums.Gender;
import users.Student;

import java.time.LocalDate;

public class DatabaseTest {

    public static void runAll() {
        TestRunner.runTest("DatabaseTest: Singleton Property", DatabaseTest::testSingleton);
        TestRunner.runTest("DatabaseTest: Organization Management", DatabaseTest::testOrganizations);
        TestRunner.runTest("DatabaseTest: Pinned News Sorting", DatabaseTest::testNewsPinning);
    }

    private static void testSingleton() {
        Database db1 = Database.getInstance();
        Database db2 = Database.getInstance();
        Assert.assertEquals(db1, db2, "Database instances must be identical (Singleton)");
    }

    private static void testOrganizations() {
        Database db = Database.getInstance();
        Student head = new Student("A", "B", "head1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.BACHELOR, 1);
        Student member = new Student("C", "D", "mem1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, DegreeType.BACHELOR, 1);
        
        Organization org = new Organization("Coding Club", head.getUsername());
        db.addOrganization(org);
        
        Assert.assertTrue(db.findOrganizationByName("Coding Club").isPresent(), "Organization should be retrievable");
        
        Organization retrieved = db.findOrganizationByName("Coding Club").get();
        retrieved.addMember(member.getUsername());
        
        Assert.assertTrue(retrieved.isMember("mem1"), "Member should be added");
        Assert.assertFalse(retrieved.isMember("fake"), "Fake member should not be found");
    }

    private static void testNewsPinning() {
        Database db = Database.getInstance();
        News n1 = new News("Normal News", "abc", "admin");
        News n2 = new News("Research: AI Breakthrough", "abc", "admin"); // Auto-pinned because it starts with "Research:"
        News n3 = new News("Another Normal", "abc", "admin");

        db.addNews(n1);
        db.addNews(n2);
        db.addNews(n3);

        var allNews = db.getAllNews(); // Should be sorted automatically
        Assert.assertEquals("Research: AI Breakthrough", allNews.get(0).getTitle(), "Pinned news must appear first");
    }
}
