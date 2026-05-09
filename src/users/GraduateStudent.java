package users;

import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import enums.DegreeType;
import enums.Faculty;
import enums.Gender;
import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import interfaces.Researcher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Master or PhD student. Always a Researcher (per requirements).
 *
 * Has a research supervisor (another Researcher with h-index >= 3).
 *
 * Design note: supervisor is stored by username to avoid circular references.
 * We removed isAdvisor from Teacher — graduate students simply hold their
 * supervisor's username and validate via the Researcher interface.
 */
public class GraduateStudent extends Student implements Researcher {

    private String supervisorUsername;   // must be a Researcher with h-index >= 3
    private final List<ResearchPaper> myPapers = new ArrayList<>();
    private final List<ResearchProject> myProjects = new ArrayList<>();

    public GraduateStudent(String firstName, String lastName, String username,
                           String password, Gender gender, LocalDate dateOfBirth,
                           String email, Faculty faculty, DegreeType degreeType,
                           int studyYear) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, degreeType, studyYear);
        if (degreeType == DegreeType.BACHELOR) {
            throw new IllegalArgumentException("GraduateStudent must be MASTER or DOCTORATE.");
        }
    }

    /**
     * Assigns a supervisor.
     * @throws LowHIndexException if the supervisor has h-index < 3
     */
    public void setSupervisor(Researcher supervisor) throws LowHIndexException {
        if (supervisor.calculateHIndex() < 3) {
            throw new LowHIndexException(
                    "Supervisor's h-index is " + supervisor.calculateHIndex() +
                    " — must be at least 3.");
        }
        // store username from the supervisor object
        try {
            this.supervisorUsername = (String)
                    supervisor.getClass().getMethod("getUsername").invoke(supervisor);
        } catch (Exception e) {
            this.supervisorUsername = supervisor.toString();
        }
        System.out.println("Supervisor set to: " + supervisorUsername);
    }

    // ── Researcher ────────────────────────────────────────────────

    @Override
    public List<ResearchPaper> getMyPapers() { return Collections.unmodifiableList(myPapers); }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        myPapers.add(paper);
        System.out.println("Paper added: " + paper.getTitle());
    }

    @Override
    public void createResearchProject(ResearchProject project) {
        myProjects.add(project);
        Database.getInstance().addResearchProject(project);
        System.out.println("Research project created: " + project.getJournalName());
    }

    @Override
    public List<ResearchProject> getResearchProjects() { return Collections.unmodifiableList(myProjects); }

    // ── research menu extension ───────────────────────────────────

    public void viewResearchCabinet() {
        System.out.println("\n=== RESEARCH CABINET ===");
        System.out.println("My papers (" + myPapers.size() + "):");
        myPapers.forEach(System.out::println);
        System.out.println("My projects (" + myProjects.size() + "):");
        myProjects.forEach(System.out::println);
        System.out.println("H-index: " + calculateHIndex());
        System.out.println("Supervisor: " + (supervisorUsername != null ? supervisorUsername : "none"));
    }

    public void addPaperInteractive() {
        System.out.print("Paper title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Journal name: ");
        String journal = scanner.nextLine().trim();
        System.out.print("Abstract/wording: ");
        String wording = scanner.nextLine().trim();
        ResearchPaper paper = new ResearchPaper(title, getUsername(), journal, wording);
        // add to matching project if exists
        Database.getInstance().findProjectByJournal(journal).ifPresent(proj -> {
            proj.publishPaper(paper);
        });
        addResearchPaper(paper);
    }

    public void joinProjectInteractive() {
        System.out.println("Available projects:");
        Database.getInstance().getResearchProjects().forEach(System.out::println);
        System.out.print("Enter journal name of project to join: ");
        String journal = scanner.nextLine().trim();
        Database.getInstance().findProjectByJournal(journal).ifPresentOrElse(proj -> {
            try {
                proj.addParticipant(this);
                System.out.println("Joined project: " + journal);
            } catch (NotResearcherException e) {
                System.out.println(e.getMessage());
            }
        }, () -> System.out.println("Project not found."));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    \n=== GRADUATE STUDENT MENU ===
                    1-14. (All student options)
                    15. Research cabinet
                    16. Add research paper
                    17. Create research project
                    18. Join research project
                    19. Print papers (sorted)
                    0. Log out""");
            System.out.print("> ");
            int choice = readInt();
            if (choice >= 1 && choice <= 14) {
                // delegate to student menu items
                handleStudentChoice(choice);
            } else {
                switch (choice) {
                    case 15 -> viewResearchCabinet();
                    case 16 -> addPaperInteractive();
                    case 17 -> createProjectInteractive();
                    case 18 -> joinProjectInteractive();
                    case 19 -> printPapersInteractive();
                    case 0, -2 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            }
        }
    }

    private void handleStudentChoice(int choice) {
        // re-use student menu by temporarily delegating
        super.showMenu(); // simplification: student menu handles its own loop
    }

    private void createProjectInteractive() {
        System.out.print("Journal name: ");
        String journal = scanner.nextLine().trim();
        System.out.print("Topic: ");
        String topic = scanner.nextLine().trim();
        ResearchProject project = new ResearchProject(journal, topic, getUsername());
        createResearchProject(project);
    }

    private void printPapersInteractive() {
        System.out.println("Sort by: 1.Citations  2.Length  3.Date  4.Title");
        switch (readInt()) {
            case 1 -> printPapers(common.PaperComparators.BY_CITATIONS);
            case 2 -> printPapers(common.PaperComparators.BY_LENGTH);
            case 3 -> printPapers(common.PaperComparators.BY_DATE);
            case 4 -> printPapers(common.PaperComparators.BY_TITLE);
            default -> System.out.println("Invalid.");
        }
    }

    public String getSupervisorUsername() { return supervisorUsername; }
}
