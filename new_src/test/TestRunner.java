package test;

import java.util.ArrayList;
import java.util.List;

public final class TestRunner {
    public interface TestCase { void run() throws Exception; }

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static final List<String> failed = new ArrayList<>();

    public static void run(String name, TestCase test) {
        testsRun++;
        System.out.println("Running: " + name + "...");
        try {
            test.run();
            testsPassed++;
            System.out.println("  [OK]");
        } catch (Exception e) {
            failed.add(name + " -> " + e.getMessage());
            System.out.println("  [FAIL] " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   NEW ARCHITECTURE TEST SUITE           ");
        System.out.println("=========================================\n");
        Assert.reset();

        EnrollmentRulesTest.runAll();
        EnrollmentServiceTest.runAll();
        BecomeResearcherTest.runAll();
        RecordMarksTest.runAll();
        ResearcherSupportTest.runAll();
        RoomSchedulerTest.runAll();
        PaperPublisherTest.runAll();
        AuthenticationTest.runAll();
        JsonPersistenceTest.runAll();
        QueryBuilderTest.runAll();
        NewUseCasesTest.runAll();

        System.out.println("\n=========================================");
        System.out.println("Tests run:    " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + (testsRun - testsPassed));
        System.out.println("Assertions:   " + Assert.passed() + " ok, " + Assert.failed() + " failed");
        if (!failed.isEmpty()) {
            System.out.println("\nFAILURES:");
            failed.forEach(f -> System.out.println(" - " + f));
            System.exit(1);
        } else {
            System.out.println("\n[SUCCESS] All tests passed.");
            System.exit(0);
        }
    }
}
