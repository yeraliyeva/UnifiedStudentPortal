package users;

import common.LogManager;
import common.Messages;
import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import education.Book;
import education.Course;
import enums.Faculty;
import enums.Gender;
import enums.TeacherPosition;
import enums.UrgencyLevel;
import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import interfaces.CanBorrowBook;
import interfaces.Researcher;
import interfaces.Subscriber;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Teacher extends Employee implements Researcher, CanBorrowBook, Subscriber {

    private String degree;
    private TeacherPosition position;
    private final List<Course> taughtCourses = new ArrayList<>();
    private final List<ResearchPaper> myPapers = new ArrayList<>();
    private final List<ResearchProject> myProjects = new ArrayList<>();
    private final List<Book> borrowedBooks = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();
    private final java.util.Set<String> subscribedJournals = new java.util.LinkedHashSet<>();
    private final List<Integer> ratings = new ArrayList<>();

    public Teacher(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty, double salary,
                   LocalDate hireDate, String insuranceNumber,
                   String degree, TeacherPosition position) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
        this.degree = degree;
        this.position = position;
    }

    public Teacher(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty, double salary,
                   LocalDate hireDate, String insuranceNumber,
                   String degree, boolean isProfessor) {
        this(firstName, lastName, username, password, gender, dateOfBirth,
             email, faculty, salary, hireDate, insuranceNumber,
             degree, isProfessor ? TeacherPosition.PROFESSOR : TeacherPosition.LECTOR);
    }

    public void assignToCourse(Course course) {
        if (!taughtCourses.contains(course)) {
            taughtCourses.add(course);
            course.addTeacher(this);
        }
    }

    public void viewTaughtCourses() {
        System.out.println(Messages.get("teacher.courses.title"));
        if (taughtCourses.isEmpty()) { System.out.println(Messages.get("teacher.courses.empty")); return; }
        taughtCourses.forEach(c -> System.out.println(c + " | " + Messages.fmt("teacher.courses.students", c.getStudents().size())));
    }

    public void putMarksInteractive() {
        viewTaughtCourses();
        System.out.print(Messages.get("teacher.courses.enter_id"));
        String cid = scanner.nextLine().trim();
        taughtCourses.stream().filter(c -> c.getCourseId().equals(cid)).findFirst().ifPresentOrElse(course -> {
            if (course.getStudents().isEmpty()) { System.out.println(Messages.get("teacher.courses.no_students")); return; }
            for (Student s : course.getStudents()) {
                System.out.println(Messages.fmt("teacher.marks.student", s.getUsername()));
                System.out.print(Messages.get("teacher.marks.att1")); int a1 = readInt();
                System.out.print(Messages.get("teacher.marks.att2")); int a2 = readInt();
                System.out.print(Messages.get("teacher.marks.exam")); int ex = readInt();
                course.setGrade(s.getUsername(), a1, a2, ex);
                int total = a1 + a2 + ex;
                if (total < 50) {
                    s.recordFail();
                    System.out.println(Messages.fmt("teacher.marks.fail", total, s.getFailCount()));
                } else {
                    s.markCourseCompleted(course);
                    System.out.println(Messages.fmt("teacher.marks.pass", total,
                            course.getGrade(s.getUsername()).getLetterGrade()));
                }
                LogManager.getInstance().log(getUsername(), "Put marks for " + s.getUsername()
                        + " in " + course.getCourseName() + ": " + a1 + "/" + a2 + "/" + ex);
            }
        }, () -> System.out.println(Messages.get("teacher.courses.not_found")));
    }

    public void viewStudentAttestations() {
        viewTaughtCourses();
        System.out.print(Messages.get("teacher.courses.enter_id"));
        String cid = scanner.nextLine().trim();
        taughtCourses.stream().filter(c -> c.getCourseId().equals(cid)).findFirst().ifPresentOrElse(course -> {
            System.out.println(Messages.fmt("teacher.grades.title", course.getCourseName()));
            course.getAllGrades().forEach((username, grade) ->
                    System.out.println("  " + username + ": " + grade));
        }, () -> System.out.println(Messages.get("teacher.courses.not_found")));
    }

    public void viewOfficeHoursSchedule() {
        System.out.println(Messages.get("teacher.office.title"));
        taughtCourses.forEach(c ->
            c.getLessons().stream()
                .filter(l -> l.getType() == enums.LessonType.OFFICE_HOURS)
                .forEach(l -> System.out.println(c.getCourseName() + ": " + l))
        );
    }

    public void sendComplaintAboutStudent(String studentUsername, String reason, UrgencyLevel urgency) {
        Database.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Dean && u.getFaculty() == getFaculty())
                .findFirst()
                .ifPresentOrElse(
                        dean -> {
                            sendMessage(dean.getUsername(),
                                    "Complaint about student: " + studentUsername,
                                    reason, urgency);
                            LogManager.getInstance().log(getUsername(),
                                    "Sent complaint about " + studentUsername + " (urgency: " + urgency + ")");
                            System.out.println(Messages.fmt("teacher.complaint.sent", dean.getUsername()));
                        },
                        () -> System.out.println(Messages.fmt("teacher.complaint.no_dean", getFaculty())));
    }

    public void addRating(int rating) {
        ratings.add(Math.max(1, Math.min(10, rating)));
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    public List<Integer> getRatings() { return Collections.unmodifiableList(ratings); }

    @Override
    public List<ResearchPaper> getMyPapers() { return Collections.unmodifiableList(myPapers); }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        myPapers.add(paper);
        LogManager.getInstance().log(getUsername(), "Added research paper: " + paper.getTitle());
        System.out.println(Messages.fmt("teacher.paper.added", paper.getTitle()));
    }

    @Override
    public void createResearchProject(ResearchProject project) {
        myProjects.add(project);
        Database.getInstance().addResearchProject(project);
        LogManager.getInstance().log(getUsername(), "Created research project: " + project.getJournalName());
        System.out.println(Messages.fmt("teacher.project.created", project.getJournalName()));
    }

    @Override
    public List<ResearchProject> getResearchProjects() { return Collections.unmodifiableList(myProjects); }

    public void viewResearchCabinet() {
        System.out.println(Messages.get("teacher.research.title"));
        System.out.println(Messages.fmt("teacher.research.position", position, degree));
        System.out.println(Messages.fmt("teacher.research.stats", myPapers.size(), calculateHIndex()));
        myPapers.forEach(System.out::println);
        System.out.println(Messages.fmt("teacher.research.projects", myProjects.size()));
        myProjects.forEach(System.out::println);
    }

    @Override
    public void borrowBook(Book book) {
        if (book.isBorrowed()) { System.out.println(Messages.get("student.book.already_borrowed")); return; }
        book.setBorrowed(true);
        borrowedBooks.add(book);
        System.out.println(Messages.fmt("student.book.borrowed", book.getTitle()));
    }

    @Override
    public void returnBook(Book book) {
        if (!borrowedBooks.contains(book)) { System.out.println(Messages.get("student.book.not_yours")); return; }
        borrowedBooks.remove(book);
        book.setBorrowed(false);
        System.out.println(Messages.fmt("student.book.returned", book.getTitle()));
    }

    @Override
    public void notifyNewPaper(String journalName, ResearchPaper paper) {
        String note = "[JOURNAL] New paper in '" + journalName + "': " + paper.getTitle();
        notifications.add(note);
        System.out.println(note);
    }

    public void viewNotifications() {
        System.out.println(Messages.get("user.notif.title"));
        if (notifications.isEmpty()) { System.out.println(Messages.get("user.notif.empty")); return; }
        notifications.forEach(System.out::println);
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
            System.out.println(Messages.get("teacher.menu.title"));
            System.out.println("1. " + Messages.get("teacher.menu.1"));
            System.out.println("2. " + Messages.get("teacher.menu.2"));
            System.out.println("3. " + Messages.get("teacher.menu.3"));
            System.out.println("4. " + Messages.get("teacher.menu.4"));
            System.out.println("5. " + Messages.get("teacher.menu.5"));
            System.out.println("6. " + Messages.get("teacher.menu.6"));
            System.out.println("7. " + Messages.get("teacher.menu.7"));
            System.out.println("8. " + Messages.get("teacher.menu.8"));
            System.out.println("9. " + Messages.get("teacher.menu.9"));
            System.out.println("10. " + Messages.get("teacher.menu.10"));
            System.out.println("11. " + Messages.get("teacher.menu.11"));
            System.out.println("12. " + Messages.get("teacher.menu.12"));
            System.out.println("13. " + Messages.get("teacher.menu.13"));
            System.out.println("14. " + Messages.get("teacher.menu.14"));
            System.out.println("15. " + Messages.get("teacher.menu.15"));
            System.out.println("16. " + Messages.get("teacher.menu.16"));
            System.out.println("17. " + Messages.get("teacher.menu.17"));
            System.out.println("18. " + Messages.get("teacher.menu.18"));
            System.out.println("19. " + Messages.get("teacher.menu.19"));
            System.out.println("20. " + Messages.get("teacher.menu.20"));
            System.out.println("21. " + Messages.get("teacher.menu.21"));
            System.out.println("22. " + Messages.get("teacher.menu.22"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            switch (readInt()) {
                case 1  -> viewTaughtCourses();
                case 2  -> putMarksInteractive();
                case 3  -> viewStudentAttestations();
                case 4  -> viewLessonSchedule();
                case 5  -> viewOfficeHoursSchedule();
                case 6  -> complaintInteractive();
                case 7  -> viewInbox();
                case 8  -> sendMessageInteractive();
                case 9  -> viewResearchCabinet();
                case 10 -> addPaperInteractive();
                case 11 -> createProjectInteractive();
                case 12 -> printPapersInteractive();
                case 13 -> viewNews();
                case 14 -> borrowBookInteractive();
                case 15 -> viewPersonalInfo();
                case 16 -> { System.out.print(Messages.get("employee.order.desc")); addOrder(scanner.nextLine().trim()); }
                case 17 -> {
                    System.out.println(Messages.fmt("teacher.ratings.list", ratings));
                    System.out.println(Messages.fmt("teacher.ratings.avg", String.format("%.1f", getAverageRating())));
                }
                case 18 -> generateCitationInteractive();
                case 19 -> subscribeJournalInteractive();
                case 20 -> unsubscribeJournalInteractive();
                case 21 -> viewSubscriptions();
                case 22 -> viewNotifications();
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }

    private void viewLessonSchedule() {
        System.out.println(Messages.get("student.schedule.title"));
        taughtCourses.forEach(c -> {
            System.out.println("  " + c.getCourseName() + ":");
            c.getLessons().forEach(l -> System.out.println("    " + l));
        });
    }

    private void complaintInteractive() {
        System.out.print(Messages.get("teacher.complaint.student"));
        String sUsername = scanner.nextLine().trim();
        System.out.print(Messages.get("teacher.complaint.reason"));
        String reason = scanner.nextLine().trim();
        System.out.print(Messages.get("user.msg.urgency"));
        UrgencyLevel urgency = parseUrgency(scanner.nextLine().trim());
        sendComplaintAboutStudent(sUsername, reason, urgency);
    }

    private void addPaperInteractive() {
        System.out.print(Messages.get("teacher.paper.title"));
        String title = scanner.nextLine().trim();
        System.out.print(Messages.get("teacher.paper.journal"));
        String journal = scanner.nextLine().trim();
        System.out.print(Messages.get("teacher.paper.abstract"));
        String wording = scanner.nextLine().trim();
        System.out.print(Messages.get("teacher.paper.pages"));
        int pages = readInt();
        System.out.print(Messages.get("teacher.paper.doi"));
        String doi = scanner.nextLine().trim();
        ResearchPaper paper = new ResearchPaper(title, getUsername(), journal, wording,
                pages > 0 ? pages : 0, doi.isEmpty() ? null : doi);
        Database.getInstance().findProjectByJournal(journal).ifPresent(proj -> proj.publishPaper(paper));
        addResearchPaper(paper);
    }

    private void createProjectInteractive() {
        System.out.print(Messages.get("teacher.project.journal"));
        String journal = scanner.nextLine().trim();
        System.out.print(Messages.get("teacher.project.topic"));
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
            default -> System.out.println(Messages.get("common.invalid"));
        }
    }

    private void borrowBookInteractive() {
        Database.getInstance().getBooks().stream().filter(b -> !b.isBorrowed()).forEach(System.out::println);
        System.out.print(Messages.get("teacher.book.title"));
        String title = scanner.nextLine().trim();
        Database.getInstance().findBookByTitle(title).ifPresentOrElse(
                this::borrowBook, () -> System.out.println(Messages.get("teacher.book.not_available")));
    }

    public String getDegree() { return degree; }
    public boolean isProfessor() { return position == TeacherPosition.PROFESSOR; }
    public TeacherPosition getPosition() { return position; }
    public void setPosition(TeacherPosition position) { this.position = position; }
    public List<Course> getTaughtCourses() { return Collections.unmodifiableList(taughtCourses); }
}
