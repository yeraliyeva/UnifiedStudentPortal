package users;

import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import enums.Faculty;
import enums.Gender;
import interfaces.Researcher;
import interfaces.Subscriber;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An employee who is neither a Teacher nor Student but performs research.
 * Directly implements Researcher (no need for decorator here since this
 * is a concrete role, not a dynamic add-on).
 */
public class EmployeeResearcher extends Employee implements Researcher, Subscriber {

    private String researchField;
    private final List<ResearchPaper> papers = new ArrayList<>();
    private final List<ResearchProject> projects = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();

    public EmployeeResearcher(String firstName, String lastName, String username,
                              String password, Gender gender, LocalDate dateOfBirth,
                              String email, Faculty faculty, double salary,
                              LocalDate hireDate, String insuranceNumber,
                              String researchField) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
        this.researchField = researchField;
    }

    // ── Researcher ────────────────────────────────────────────────

    @Override
    public List<ResearchPaper> getMyPapers() { return Collections.unmodifiableList(papers); }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        papers.add(paper);
        System.out.println("Paper added: " + paper.getTitle());
    }

    @Override
    public void createResearchProject(ResearchProject project) {
        projects.add(project);
        Database.getInstance().addResearchProject(project);
        System.out.println("Project created: " + project.getJournalName());
    }

    @Override
    public List<ResearchProject> getResearchProjects() { return Collections.unmodifiableList(projects); }

    // ── Subscriber ────────────────────────────────────────────────

    @Override
    public void notifyNewPaper(String journalName, ResearchPaper paper) {
        String note = "[JOURNAL] New paper in " + journalName + ": " + paper.getTitle();
        notifications.add(note);
        System.out.println(note);
    }

    public void viewResearchCabinet() {
        System.out.println("\n=== RESEARCH CABINET ===");
        System.out.println("Field: " + researchField);
        System.out.println("Papers (" + papers.size() + "):"); papers.forEach(System.out::println);
        System.out.println("H-index: " + calculateHIndex());
        System.out.println("Projects (" + projects.size() + "):"); projects.forEach(System.out::println);
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== EMPLOYEE RESEARCHER MENU ===
                    1. Research cabinet
                    2. Add paper
                    3. Create project
                    4. Print papers
                    5. View inbox
                    6. Send message
                    7. View news
                    8. Personal info
                    0. Log out""");
            System.out.print("> ");
            switch (readInt()) {
                case 1 -> viewResearchCabinet();
                case 2 -> addPaperInteractive();
                case 3 -> createProjectInteractive();
                case 4 -> {
                    System.out.println("Sort: 1.Citations  2.Date  3.Title");
                    switch (readInt()) {
                        case 1 -> printPapers(common.PaperComparators.BY_CITATIONS);
                        case 2 -> printPapers(common.PaperComparators.BY_DATE);
                        case 3 -> printPapers(common.PaperComparators.BY_TITLE);
                    }
                }
                case 5 -> viewInbox();
                case 6 -> sendMessageInteractive();
                case 7 -> viewNews();
                case 8 -> viewPersonalInfo();
                case 0, -2 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void addPaperInteractive() {
        System.out.print("Title: ");      String title   = scanner.nextLine().trim();
        System.out.print("Journal: ");    String journal = scanner.nextLine().trim();
        System.out.print("Abstract: ");   String wording = scanner.nextLine().trim();
        ResearchPaper paper = new ResearchPaper(title, getUsername(), journal, wording);
        Database.getInstance().findProjectByJournal(journal).ifPresent(p -> p.publishPaper(paper));
        addResearchPaper(paper);
    }

    private void createProjectInteractive() {
        System.out.print("Journal: "); String journal = scanner.nextLine().trim();
        System.out.print("Topic: ");   String topic   = scanner.nextLine().trim();
        createResearchProject(new ResearchProject(journal, topic, getUsername()));
    }

    public String getResearchField() { return researchField; }
    public void setResearchField(String researchField) { this.researchField = researchField; }
}
