package users;

import common.LogManager;
import common.Messages;
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

public class EmployeeResearcher extends Employee implements Researcher, Subscriber {

    private String researchField;
    private final List<ResearchPaper> myPapers = new ArrayList<>();
    private final List<ResearchProject> myProjects = new ArrayList<>();
    private final java.util.Set<String> subscribedJournals = new java.util.LinkedHashSet<>();

    public EmployeeResearcher(String firstName, String lastName, String username,
                              String password, Gender gender, LocalDate dateOfBirth,
                              String email, Faculty faculty, double salary,
                              LocalDate hireDate, String insuranceNumber, String researchField) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
        this.researchField = researchField;
    }

    @Override
    public List<ResearchPaper> getMyPapers() {
        return Collections.unmodifiableList(myPapers);
    }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        myPapers.add(paper);
        LogManager.getInstance().log(getUsername(), "Added research paper: " + paper.getTitle());
        System.out.println(Messages.fmt("research.paper.added", paper.getTitle()));
    }

    @Override
    public void createResearchProject(ResearchProject project) {
        myProjects.add(project);
        Database.getInstance().addResearchProject(project);
        LogManager.getInstance().log(getUsername(), "Created project: " + project.getJournalName());
        System.out.println(Messages.fmt("research.project.created", project.getJournalName()));
    }

    @Override
    public List<ResearchProject> getResearchProjects() {
        return Collections.unmodifiableList(myProjects);
    }

    public void viewResearchCabinet() {
        System.out.println(Messages.get("empres.research.title"));
        System.out.println(Messages.fmt("empres.research.field", researchField));
        System.out.println(Messages.fmt("empres.research.papers", myPapers.size()));
        myPapers.forEach(System.out::println);
        System.out.println(Messages.fmt("empres.research.hindex", calculateHIndex()));
        System.out.println(Messages.fmt("empres.research.projects", myProjects.size()));
        myProjects.forEach(System.out::println);
    }

    public void addPaperInteractive() {
        System.out.print(Messages.get("empres.paper.title"));
        String title = scanner.nextLine().trim();
        System.out.print(Messages.get("empres.paper.journal"));
        String journal = scanner.nextLine().trim();
        System.out.print(Messages.get("empres.paper.abstract"));
        String wording = scanner.nextLine().trim();
        ResearchPaper paper = new ResearchPaper(title, getUsername(), journal, wording, 0, null);
        Database.getInstance().findProjectByJournal(journal).ifPresent(proj -> proj.publishPaper(paper));
        addResearchPaper(paper);
    }

    public void createProjectInteractive() {
        System.out.print(Messages.get("research.project.journal"));
        String journal = scanner.nextLine().trim();
        System.out.print(Messages.get("research.project.topic"));
        String topic = scanner.nextLine().trim();
        ResearchProject project = new ResearchProject(journal, topic, getUsername());
        createResearchProject(project);
    }

    public void printPapersInteractive() {
        System.out.println(Messages.get("empres.sort.prompt"));
        switch (readInt()) {
            case 1 -> printPapers(common.PaperComparators.BY_CITATIONS);
            case 2 -> printPapers(common.PaperComparators.BY_DATE);
            case 3 -> printPapers(common.PaperComparators.BY_TITLE);
            default -> System.out.println(Messages.get("common.invalid"));
        }
    }

    @Override
    public void notifyNewPaper(String journalName, ResearchPaper paper) {
        System.out.println("[JOURNAL NOTIFICATION] New paper in '" + journalName + "': " + paper.getTitle());
    }

    public void generateCitationInteractive() {
        if (myPapers.isEmpty()) { System.out.println(Messages.get("citation.no_papers")); return; }
        myPapers.forEach(System.out::println);
        System.out.print(Messages.get("citation.paper_id"));
        int pid = readInt();
        myPapers.stream().filter(p -> p.getId() == pid).findFirst().ifPresentOrElse(p -> {
            System.out.println(Messages.get("citation.format"));
            int fmt = readInt();
            enums.PaperFormat format = (fmt == 2) ? enums.PaperFormat.BIBTEX : enums.PaperFormat.PLAIN_TEXT;
            System.out.println(p.getCitation(format));
            LogManager.getInstance().log(getUsername(), "Generated citation for paper #" + pid);
        }, () -> System.out.println(Messages.get("citation.paper_not_found")));
    }

    public void subscribeJournalInteractive() {
        Database.getInstance().getResearchProjects().forEach(System.out::println);
        System.out.print(Messages.get("subscription.journal_prompt"));
        String journal = scanner.nextLine().trim();
        Database.getInstance().findProjectByJournal(journal).ifPresentOrElse(proj -> {
            proj.subscribe(this);
            subscribedJournals.add(journal);
            LogManager.getInstance().log(getUsername(), "Subscribed to journal: " + journal);
            System.out.println(Messages.fmt("subscription.subscribed", journal));
        }, () -> System.out.println(Messages.fmt("subscription.journal_not_found", journal)));
    }

    public void unsubscribeJournalInteractive() {
        viewSubscriptions();
        System.out.print(Messages.get("subscription.unsub_prompt"));
        String journal = scanner.nextLine().trim();
        Database.getInstance().findProjectByJournal(journal).ifPresent(proj -> {
            proj.unsubscribe(this);
            subscribedJournals.remove(journal);
            LogManager.getInstance().log(getUsername(), "Unsubscribed from journal: " + journal);
            System.out.println(Messages.fmt("subscription.unsubscribed", journal));
        });
    }

    public void viewSubscriptions() {
        System.out.println(Messages.get("subscription.title"));
        if (subscribedJournals.isEmpty()) { System.out.println(Messages.get("subscription.empty")); return; }
        subscribedJournals.forEach(j -> System.out.println("  - " + j));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("empres.menu.title"));
            System.out.println("1. " + Messages.get("empres.menu.1"));
            System.out.println("2. " + Messages.get("empres.menu.2"));
            System.out.println("3. " + Messages.get("empres.menu.3"));
            System.out.println("4. " + Messages.get("empres.menu.4"));
            System.out.println("5. " + Messages.get("empres.menu.5"));
            System.out.println("6. " + Messages.get("empres.menu.6"));
            System.out.println("7. " + Messages.get("empres.menu.7"));
            System.out.println("8. " + Messages.get("empres.menu.8"));
            System.out.println("9. " + Messages.get("empres.menu.9"));
            System.out.println("10. " + Messages.get("empres.menu.10"));
            System.out.println("11. " + Messages.get("empres.menu.11"));
            System.out.println("12. " + Messages.get("empres.menu.12"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            switch (readInt()) {
                case 1  -> viewResearchCabinet();
                case 2  -> addPaperInteractive();
                case 3  -> createProjectInteractive();
                case 4  -> printPapersInteractive();
                case 5  -> viewInbox();
                case 6  -> sendMessageInteractive();
                case 7  -> viewNews();
                case 8  -> { viewPersonalInfo(); editPersonalInfo(); }
                case 9  -> generateCitationInteractive();
                case 10 -> subscribeJournalInteractive();
                case 11 -> unsubscribeJournalInteractive();
                case 12 -> viewSubscriptions();
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }

    public String getResearchField() { return researchField; }
    public void setResearchField(String researchField) { this.researchField = researchField; }
}
