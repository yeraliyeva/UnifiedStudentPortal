package test;

import data.Database;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    public interface TestCase {
        void run() throws Exception;
    }

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static final List<String> failedTests = new ArrayList<>();

    public static void runTest(String testName, TestCase test) {
        testsRun++;
        System.out.println("Running: " + testName + "...");
        
        // Reset DB before every test to ensure isolation
        Database.resetInstanceForTesting();
        
        try {
            test.run();
            testsPassed++;
            System.out.println("  [OK]");
        } catch (Exception e) {
            System.out.println("  [FAIL] " + e.getMessage());
            failedTests.add(testName + " -> " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   UNIVERSITY SYSTEM TEST SUITE RUNNER   ");
        System.out.println("=========================================\n");

        Assert.reset();

        // 1. UNIT TESTS
        StudentTest.runAll();
        ResearchTest.runAll();
        DatabaseTest.runAll();
        ManagerAdminTest.runAll();
        EnrollmentChecksTest.runAll();
        AcademicExtrasTest.runAll();

        // 2. E2E INTEGRATION TESTS
        E2EAcademicFlowTest.runAll();
        E2EResearchFlowTest.runAll();
        E2ESupportFlowTest.runAll();

        // REPORT
        System.out.println("\n=========================================");
        System.out.println("TEST RUN SUMMARY");
        System.out.println("=========================================");
        System.out.println("Tests Run:      " + testsRun);
        System.out.println("Tests Passed:   " + testsPassed);
        System.out.println("Tests Failed:   " + (testsRun - testsPassed));
        System.out.println("Assertions:     " + Assert.getPassed() + " passed, " + Assert.getFailed() + " failed.");

        if (!failedTests.isEmpty()) {
            System.out.println("\nFAILURES:");
            for (String f : failedTests) {
                System.out.println(" - " + f);
            }
            System.exit(1);
        } else {
            System.out.println("\n[SUCCESS] All tests passed perfectly!");
            System.exit(0);
        }
    }
}
