package users;

import common.Messages;
import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import enums.DegreeType;
import enums.Faculty;
import enums.Gender;
import enums.HelpType;
import enums.UrgencyLevel;
import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import interfaces.Researcher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraduateStudent extends Student implements Researcher {

    private String supervisorUsername;
    private final List<ResearchPaper> myPapers = new ArrayList<>();
    private final List<ResearchProject> myProjects = new ArrayList<>();
    private final List<ResearchPaper> diplomaPapers = new ArrayList<>();

    public GraduateStudent(String firstName, String lastName, String username,
                           String password, Gender gender, LocalDate dateOfBirth,
                           String email, Faculty faculty, DegreeType degreeType,
                           int studyYear) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, degreeType, studyYear);
        if (degreeType == DegreeType.BACHELOR) {
            throw new IllegalArgumentException(Messages.get("grad.must_be_grad"));
        }
    }

    public void setSupervisor(Researcher supervisor) throws LowHIndexException {
        if (supervisor.calculateHIndex() < 3) {
            throw new LowHIndexException("Supervisor's h-index is " + supervisor.calculateHIndex() + " — must be at least 3.");
        }
        try {
            this.supervisorUsername = (String) supervisor.getClass().getMethod("getUsername").invoke(supervisor);
        } catch (Exception e) {
            this.supervisorUsername = supervisor.toString();
        }
    }

    public void addDiplomaPaper(ResearchPaper paper) {
        diplomaPapers.add(paper);
        System.out.println(Messages.fmt("grad.paper.diploma_added", paper.getTitle()));
    }

    @Override
    public List<ResearchPaper> getMyPapers() { return Collections.unmodifiableList(myPapers); }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        myPapers.add(paper);
        System.out.println(Messages.fmt("research.paper.added", paper.getTitle()));
    }

    @Override
    public void createResearchProject(ResearchProject project) {
        myProjects.add(project);
        Database.getInstance().addResearchProject(project);
        System.out.println(Messages.fmt("research.project.created", project.getJournalName()));
    }

    @Override
    public List<ResearchProject> getResearchProjects() { return Collections.unmodifiableList(myProjects); }

    public void viewResearchCabinet() {
        System.out.println(Messages.get("grad.research.title"));
        System.out.println(Messages.fmt("grad.papers", myPapers.size()));
        myPapers.forEach(System.out::println);
        System.out.println(Messages.fmt("grad.projects", myProjects.size()));
        myProjects.forEach(System.out::println);
        System.out.println(Messages.fmt("grad.diploma", diplomaPapers.size()));
        diplomaPapers.forEach(System.out::println);
        System.out.println(Messages.fmt("grad.hindex", calculateHIndex()));
        System.out.println(Messages.fmt("grad.supervisor", supervisorUsername != null ? supervisorUsername : Messages.get("grad.supervisor.none")));
    }

    public void addPaperInteractive() {
        System.out.print(Messages.get("grad.paper.title"));
        String title = scanner.nextLine().trim();
        System.out.print(Messages.get("grad.paper.journal"));
        String journal = scanner.nextLine().trim();
        System.out.print(Messages.get("grad.paper.abstract"));
        String wording = scanner.nextLine().trim();
        System.out.print(Messages.get("grad.paper.pages"));
        int pages = readInt();
        ResearchPaper paper = new ResearchPaper(title, getUsername(), journal, wording,
                pages > 0 ? pages : 0, null);
        Database.getInstance().findProjectByJournal(journal).ifPresent(proj -> proj.publishPaper(paper));
        addResearchPaper(paper);
        System.out.print(Messages.get("grad.paper.diploma_q"));
        String ans = scanner.nextLine().trim();
        if (ans.equalsIgnoreCase(Messages.get("common.yes")) || ans.equalsIgnoreCase("y")) {
            addDiplomaPaper(paper);
        }
    }

    public void joinProjectInteractive() {
        System.out.println(Messages.get("grad.project.available"));
        Database.getInstance().getResearchProjects().forEach(System.out::println);
        System.out.print(Messages.get("grad.project.join_prompt"));
        String journal = scanner.nextLine().trim();
        Database.getInstance().findProjectByJournal(journal).ifPresentOrElse(proj -> {
            try {
                proj.addParticipant(this);
                System.out.println(Messages.fmt("grad.project.joined", journal));
            } catch (NotResearcherException e) {
                System.out.println(e.getMessage());
            }
        }, () -> System.out.println(Messages.get("grad.project.not_found")));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("grad.menu.title"));
            System.out.println("1. " + Messages.get("student.menu.1"));
            System.out.println("2. " + Messages.get("student.menu.2"));
            System.out.println("3. " + Messages.get("student.menu.3"));
            System.out.println("4. " + Messages.get("student.menu.4"));
            System.out.println("5. " + Messages.get("student.menu.5"));
            System.out.println("6. " + Messages.get("student.menu.6"));
            System.out.println("7. " + Messages.get("student.menu.7"));
            System.out.println("8. " + Messages.get("student.menu.8"));
            System.out.println("9. " + Messages.get("student.menu.9"));
            System.out.println("10. " + Messages.get("student.menu.10"));
            System.out.println("11. " + Messages.get("student.menu.11"));
            System.out.println("12. " + Messages.get("student.menu.12"));
            System.out.println("13. " + Messages.get("student.menu.13"));
            System.out.println("14. " + Messages.get("student.menu.14"));
            System.out.println("15. " + Messages.get("student.menu.15"));
            System.out.println("16. " + Messages.get("student.menu.16"));
            System.out.println("17. " + Messages.get("student.menu.17"));
            System.out.println("18. " + Messages.get("student.menu.18"));
            System.out.println("19. " + Messages.get("student.menu.19"));
            System.out.println("20. " + Messages.get("student.menu.20"));
            System.out.println(Messages.get("grad.menu.research"));
            System.out.println("21. " + Messages.get("grad.menu.21"));
            System.out.println("22. " + Messages.get("grad.menu.22"));
            System.out.println("23. " + Messages.get("grad.menu.23"));
            System.out.println("24. " + Messages.get("grad.menu.24"));
            System.out.println("25. " + Messages.get("grad.menu.25"));
            System.out.println("26. " + Messages.get("grad.menu.26"));
            System.out.println("27. " + Messages.get("grad.menu.27"));
            System.out.println("28. " + Messages.get("grad.menu.28"));
            System.out.println("29. " + Messages.get("grad.menu.29"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            int choice = readInt();
            switch (choice) {
                case 1  -> viewEnrolledCourses();
                case 2  -> enrollInteractiveGrad();
                case 3  -> dropInteractiveGrad();
                case 4  -> viewTranscript();
                case 5  -> viewLessonSchedule();
                case 6  -> viewInbox();
                case 7  -> sendMessageInteractive();
                case 8  -> submitRequestInteractiveGrad();
                case 9  -> viewMyRequests();
                case 10 -> viewNews();
                case 11 -> viewNotifications();
                case 12 -> borrowBookInteractiveGrad();
                case 13 -> returnBookInteractiveGrad();
                case 14 -> { viewPersonalInfo(); editPersonalInfo(); }
                case 15 -> rateTeacherInteractive();
                case 16 -> viewTeacherInfoInteractive();
                case 17 -> viewOrganizations();
                case 18 -> joinOrganizationInteractive();
                case 19 -> createOrganizationInteractive();
                case 20 -> commentOnNews();
                case 21 -> viewResearchCabinet();
                case 22 -> addPaperInteractive();
                case 23 -> createProjectInteractive();
                case 24 -> joinProjectInteractive();
                case 25 -> printPapersInteractive();
                case 26 -> generateCitationInteractive();
                case 27 -> { System.out.print(Messages.get("subscription.journal_prompt"));
                            subscribeToJournal(scanner.nextLine().trim()); }
                case 28 -> { System.out.print(Messages.get("subscription.unsub_prompt"));
                            unsubscribeFromJournal(scanner.nextLine().trim()); }
                case 29 -> viewSubscriptions();
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }

    private void enrollInteractiveGrad() {
        viewAvailableCourses();
        System.out.print(Messages.get("student.courses.enter_id"));
        String id = scanner.nextLine().trim();
        Database.getInstance().findCourseById(id).ifPresentOrElse(
                this::enrollCourse,
                () -> System.out.println(Messages.get("student.courses.not_found")));
    }

    private void dropInteractiveGrad() {
        viewEnrolledCourses();
        System.out.print(Messages.get("student.courses.drop_id"));
        String id = scanner.nextLine().trim();
        getEnrolledCourses().stream()
                .filter(c -> c.getCourseId().equals(id))
                .findFirst()
                .ifPresentOrElse(this::dropCourse, () -> System.out.println(Messages.get("student.courses.not_enrolled")));
    }

    private void submitRequestInteractiveGrad() {
        System.out.println(Messages.get("student.request.types"));
        for (HelpType t : HelpType.values()) System.out.println("  " + t.ordinal() + ". " + t);
        System.out.print(Messages.get("student.request.choose"));
        int idx = readInt();
        HelpType type = HelpType.values()[Math.min(idx, HelpType.values().length - 1)];
        System.out.print(Messages.get("student.request.info"));
        String info = scanner.nextLine().trim();
        System.out.print(Messages.get("user.msg.urgency"));
        UrgencyLevel urgency = parseUrgency(scanner.nextLine().trim());
        submitRequest(type, urgency, info);
    }

    private void borrowBookInteractiveGrad() {
        System.out.println(Messages.get("student.book.available"));
        Database.getInstance().getBooks().stream().filter(b -> !b.isBorrowed()).forEach(System.out::println);
        System.out.print(Messages.get("student.book.title_prompt"));
        String title = scanner.nextLine().trim();
        Database.getInstance().findBookByTitle(title).ifPresentOrElse(
                this::borrowBook, () -> System.out.println(Messages.get("student.book.not_available")));
    }

    private void returnBookInteractiveGrad() {
        System.out.print(Messages.get("student.book.return_prompt"));
        String title = scanner.nextLine().trim();
        Database.getInstance().getBooks().stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(this::returnBook, () -> System.out.println(Messages.get("student.book.not_found")));
    }

    private void createProjectInteractive() {
        System.out.print(Messages.get("grad.project.journal"));
        String journal = scanner.nextLine().trim();
        System.out.print(Messages.get("grad.project.topic"));
        String topic = scanner.nextLine().trim();
        ResearchProject project = new ResearchProject(journal, topic, getUsername());
        createResearchProject(project);
    }

    private void printPapersInteractive() {
        System.out.println(Messages.get("grad.sort.prompt"));
        switch (readInt()) {
            case 1 -> printPapers(common.PaperComparators.BY_CITATIONS);
            case 2 -> printPapers(common.PaperComparators.BY_LENGTH);
            case 3 -> printPapers(common.PaperComparators.BY_DATE);
            case 4 -> printPapers(common.PaperComparators.BY_TITLE);
            default -> System.out.println(Messages.get("grad.sort.invalid"));
        }
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
        }, () -> System.out.println(Messages.get("citation.paper_not_found")));
    }

    public String getSupervisorUsername() { return supervisorUsername; }
    public List<ResearchPaper> getDiplomaPapers() { return Collections.unmodifiableList(diplomaPapers); }
}
