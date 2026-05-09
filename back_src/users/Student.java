package users;

import common.LogManager;
import common.Messages;
import communication.Organization;
import data.Database;
import education.Book;
import education.Course;
import enums.DegreeType;
import enums.Faculty;
import enums.Gender;
import enums.HelpType;
import enums.UrgencyLevel;
import interfaces.CanBorrowBook;
import interfaces.Educationable;
import interfaces.Subscriber;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student extends User implements CanBorrowBook, Educationable, Subscriber {

    private DegreeType degreeType;
    private int studyYear;
    private final List<Course> enrolledCourses = new ArrayList<>();
    private final List<Course> completedCourses = new ArrayList<>();
    private final List<Book> borrowedBooks = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();
    private final java.util.Set<String> subscribedJournals = new java.util.LinkedHashSet<>();
    private int availableCredits = 21;
    private int failCount = 0;
    private static final int MAX_FAILS = 3;

    public Student(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty, DegreeType degreeType, int studyYear) {
        super(firstName, lastName, username, password, gender, dateOfBirth, email, faculty);
        this.degreeType = degreeType;
        this.studyYear = studyYear;
    }

    public void enrollCourse(Course course) {
        if (failCount >= MAX_FAILS) {
            System.out.println(Messages.fmt("student.courses.fail_limit", failCount, MAX_FAILS));
            return;
        }
        if (enrolledCourses.contains(course)) {
            System.out.println(Messages.fmt("student.courses.already", course.getCourseName()));
            return;
        }
        if (availableCredits < course.getCredits()) {
            System.out.println(Messages.fmt("student.courses.no_credits", availableCredits, course.getCredits()));
            return;
        }
        if (!checkPrerequisites(course)) return;
        if (!checkCourseCapacity(course)) return;
        if (!checkScheduleConflict(course)) return;
        availableCredits -= course.getCredits();
        enrolledCourses.add(course);
        course.enrollStudent(this);
        LogManager.getInstance().log(getUsername(), "Enrolled in course: " + course.getCourseName());
        System.out.println(Messages.fmt("student.courses.enrolled", course.getCourseName()));
    }

    private boolean checkPrerequisites(Course course) {
        for (Course prereq : course.getPrerequisites()) {
            if (!completedCourses.contains(prereq)) {
                System.out.println(Messages.fmt("student.courses.prereq_missing",
                        course.getCourseName(), prereq.getCourseName()));
                LogManager.getInstance().log(getUsername(),
                        "Enrollment denied (missing prereq): " + course.getCourseName()
                                + " <- " + prereq.getCourseName());
                return false;
            }
        }
        return true;
    }

    private boolean checkCourseCapacity(Course course) {
        if (course.isFull()) {
            System.out.println(Messages.fmt("student.courses.full",
                    course.getCourseName(), course.getMaxStudents()));
            LogManager.getInstance().log(getUsername(),
                    "Enrollment denied (course full): " + course.getCourseName());
            return false;
        }
        return true;
    }

    private boolean checkScheduleConflict(Course course) {
        for (education.Lesson newLesson : course.getLessons()) {
            for (Course existing : enrolledCourses) {
                for (education.Lesson taken : existing.getLessons()) {
                    if (newLesson.getDay() == taken.getDay()
                            && newLesson.getTime().equals(taken.getTime())) {
                        System.out.println(Messages.fmt("student.courses.schedule_conflict",
                                course.getCourseName(), existing.getCourseName(),
                                taken.getDay(), taken.getTime()));
                        LogManager.getInstance().log(getUsername(),
                                "Enrollment denied (schedule conflict): " + course.getCourseName()
                                        + " vs " + existing.getCourseName());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void markCourseCompleted(Course course) {
        if (!completedCourses.contains(course)) {
            completedCourses.add(course);
            LogManager.getInstance().log(getUsername(), "Completed course: " + course.getCourseName());
        }
    }

    public List<Course> getCompletedCourses() { return Collections.unmodifiableList(completedCourses); }

    public List<String> getNotifications() { return Collections.unmodifiableList(notifications); }

    public java.util.Set<String> getSubscribedJournals() { return java.util.Collections.unmodifiableSet(subscribedJournals); }

    public void subscribeToJournal(String journalName) {
        Database.getInstance().findProjectByJournal(journalName).ifPresentOrElse(proj -> {
            proj.subscribe(this);
            subscribedJournals.add(journalName);
            LogManager.getInstance().log(getUsername(), "Subscribed to journal: " + journalName);
            System.out.println(Messages.fmt("subscription.subscribed", journalName));
        }, () -> System.out.println(Messages.fmt("subscription.journal_not_found", journalName)));
    }

    public void unsubscribeFromJournal(String journalName) {
        Database.getInstance().findProjectByJournal(journalName).ifPresent(proj -> {
            proj.unsubscribe(this);
            subscribedJournals.remove(journalName);
            LogManager.getInstance().log(getUsername(), "Unsubscribed from journal: " + journalName);
            System.out.println(Messages.fmt("subscription.unsubscribed", journalName));
        });
    }

    public void viewSubscriptions() {
        System.out.println(Messages.get("subscription.title"));
        if (subscribedJournals.isEmpty()) { System.out.println(Messages.get("subscription.empty")); return; }
        subscribedJournals.forEach(j -> System.out.println("  - " + j));
    }

    public void dropCourse(Course course) {
        if (!enrolledCourses.contains(course)) {
            System.out.println(Messages.get("student.courses.not_enrolled"));
            return;
        }
        enrolledCourses.remove(course);
        course.removeStudent(this);
        availableCredits += course.getCredits();
        LogManager.getInstance().log(getUsername(), "Dropped course: " + course.getCourseName());
        System.out.println(Messages.fmt("student.courses.dropped", course.getCourseName()));
    }

    public void recordFail() {
        failCount++;
        if (failCount >= MAX_FAILS) {
            System.out.println(Messages.fmt("student.fail.warning", getUsername(), MAX_FAILS));
        }
    }

    public void viewEnrolledCourses() {
        System.out.println(Messages.get("student.courses.title"));
        if (enrolledCourses.isEmpty()) { System.out.println(Messages.get("student.courses.empty")); return; }
        enrolledCourses.forEach(System.out::println);
    }

    public void viewAvailableCourses() {
        System.out.println(Messages.get("student.courses.available"));
        Database.getInstance().getCourses()
                .forEach(c -> System.out.println(c + " | " + Messages.fmt("student.courses.credits_left", availableCredits)));
    }

    public void viewAttestation() {
        System.out.println(Messages.get("student.attest.title"));
        for (Course course : enrolledCourses) {
            var grade = course.getGrade(getUsername());
            if (grade != null) {
                System.out.println(course.getCourseName() + ": " + grade);
            }
        }
    }

    public void viewTranscript() {
        System.out.println(Messages.fmt("student.transcript.title", getFirstName(), getLastName()));
        System.out.println(Messages.fmt("student.transcript.info", degreeType, studyYear, failCount));
        viewAttestation();
        System.out.println(Messages.fmt("student.transcript.gpa", String.format("%.2f", calculateGPA())));
    }

    public double calculateGPA() {
        if (enrolledCourses.isEmpty()) return 0.0;
        double total = 0;
        int count = 0;
        for (Course c : enrolledCourses) {
            var grade = c.getGrade(getUsername());
            if (grade != null) {
                total += grade.getTotal();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    public void rateTeacherInteractive() {
        System.out.println(Messages.get("student.rate.title"));
        enrolledCourses.forEach(c -> {
            System.out.println(c.getCourseName() + ":");
            c.getTeachers().forEach(t -> System.out.println("  " + t.getUsername() + " (" + t.getFirstName() + " " + t.getLastName() + ")"));
        });
        System.out.print(Messages.get("student.rate.enter_user"));
        String tUsername = scanner.nextLine().trim();
        User u = Database.getInstance().getUser(tUsername);
        if (u instanceof Teacher teacher) {
            System.out.print(Messages.get("student.rate.enter_rating"));
            int rating = readInt();
            if (rating >= 1 && rating <= 10) {
                teacher.addRating(rating);
                LogManager.getInstance().log(getUsername(), "Rated teacher " + tUsername + ": " + rating);
                System.out.println(Messages.fmt("student.rate.done", String.format("%.1f", teacher.getAverageRating())));
            } else {
                System.out.println(Messages.get("student.rate.invalid"));
            }
        } else {
            System.out.println(Messages.get("student.rate.not_found"));
        }
    }

    public void viewTeacherInfoInteractive() {
        System.out.println(Messages.get("student.teacher_info.title"));
        enrolledCourses.forEach(c -> {
            System.out.println(c.getCourseName() + ":");
            c.getTeachers().forEach(t -> {
                System.out.println("  " + t);
                System.out.println("    Degree: " + t.getDegree() + " | Professor: " + t.isProfessor());
                System.out.printf("    Rating: %.1f / 10%n", t.getAverageRating());
            });
        });
    }

    public void viewOrganizations() {
        System.out.println(Messages.get("student.org.title"));
        var orgs = Database.getInstance().getOrganizations();
        if (orgs.isEmpty()) { System.out.println(Messages.get("student.org.empty")); return; }
        orgs.forEach(o -> {
            String role = o.getHeadUsername().equals(getUsername()) ? " [HEAD]" :
                    o.isMember(getUsername()) ? " [MEMBER]" : "";
            System.out.println(o + role);
        });
    }

    public void joinOrganizationInteractive() {
        viewOrganizations();
        System.out.print(Messages.get("student.org.join_prompt"));
        String name = scanner.nextLine().trim();
        Database.getInstance().findOrganizationByName(name).ifPresentOrElse(org -> {
            if (org.isMember(getUsername())) {
                System.out.println(Messages.get("student.org.already"));
            } else {
                org.addMember(getUsername());
                LogManager.getInstance().log(getUsername(), "Joined organization: " + name);
                System.out.println(Messages.fmt("student.org.joined", name));
            }
        }, () -> System.out.println(Messages.get("student.org.not_found")));
    }

    public void createOrganizationInteractive() {
        System.out.print(Messages.get("student.org.name_prompt"));
        String name = scanner.nextLine().trim();
        if (Database.getInstance().findOrganizationByName(name).isPresent()) {
            System.out.println(Messages.get("student.org.exists"));
            return;
        }
        Organization org = new Organization(name, getUsername());
        Database.getInstance().addOrganization(org);
        LogManager.getInstance().log(getUsername(), "Created organization: " + name);
        System.out.println(Messages.fmt("student.org.created", org));
    }

    @Override
    public void viewLessonSchedule() {
        System.out.println(Messages.get("student.schedule.title"));
        for (Course course : enrolledCourses) {
            System.out.println("  " + course.getCourseName() + ":");
            course.getLessons().forEach(l -> System.out.println("    " + l));
        }
    }

    @Override
    public void viewExamsSchedule() {
        System.out.println(Messages.get("student.exams.title"));
        enrolledCourses.forEach(c -> {
            System.out.println("  " + c.getCourseName());
            c.getLessons().stream()
                    .filter(l -> l.getType() == enums.LessonType.EXAM)
                    .forEach(l -> System.out.println("    " + l));
        });
    }

    @Override
    public void viewJournal() {
        viewAttestation();
    }

    @Override
    public void attendanceMark() {
        LogManager.getInstance().log(getUsername(), "Attendance marked");
        System.out.println(Messages.fmt("student.attendance.marked", getUsername()));
    }

    @Override
    public void viewDisciplineSchedule() {
        viewLessonSchedule();
    }

    @Override
    public void borrowBook(Book book) {
        if (book.isBorrowed()) {
            System.out.println(Messages.get("student.book.already_borrowed"));
            return;
        }
        book.setBorrowed(true);
        borrowedBooks.add(book);
        LogManager.getInstance().log(getUsername(), "Borrowed book: " + book.getTitle());
        System.out.println(Messages.fmt("student.book.borrowed", book.getTitle()));
    }

    @Override
    public void returnBook(Book book) {
        if (!borrowedBooks.contains(book)) {
            System.out.println(Messages.get("student.book.not_yours"));
            return;
        }
        borrowedBooks.remove(book);
        book.setBorrowed(false);
        LogManager.getInstance().log(getUsername(), "Returned book: " + book.getTitle());
        System.out.println(Messages.fmt("student.book.returned", book.getTitle()));
    }

    @Override
    public void notifyNewPaper(String journalName, communication.ResearchPaper paper) {
        String note = "[JOURNAL NOTIFICATION] New paper in '" + journalName + "': " + paper.getTitle();
        notifications.add(note);
        System.out.println(note);
    }

    public void viewNotifications() {
        System.out.println(Messages.get("student.notif.title"));
        if (notifications.isEmpty()) { System.out.println(Messages.get("student.notif.empty")); return; }
        notifications.forEach(System.out::println);
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("student.menu.title"));
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
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            int choice = readInt();
            switch (choice) {
                case 1  -> viewEnrolledCourses();
                case 2  -> enrollInteractive();
                case 3  -> dropInteractive();
                case 4  -> viewTranscript();
                case 5  -> viewLessonSchedule();
                case 6  -> viewInbox();
                case 7  -> sendMessageInteractive();
                case 8  -> submitRequestInteractive();
                case 9  -> viewMyRequests();
                case 10 -> viewNews();
                case 11 -> viewNotifications();
                case 12 -> borrowBookInteractive();
                case 13 -> returnBookInteractive();
                case 14 -> { viewPersonalInfo(); editPersonalInfo(); }
                case 15 -> rateTeacherInteractive();
                case 16 -> viewTeacherInfoInteractive();
                case 17 -> viewOrganizations();
                case 18 -> joinOrganizationInteractive();
                case 19 -> createOrganizationInteractive();
                case 20 -> commentOnNews();
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }

    private void enrollInteractive() {
        viewAvailableCourses();
        System.out.print(Messages.get("student.courses.enter_id"));
        String id = scanner.nextLine().trim();
        Database.getInstance().findCourseById(id).ifPresentOrElse(
                this::enrollCourse,
                () -> System.out.println(Messages.get("student.courses.not_found")));
    }

    private void dropInteractive() {
        viewEnrolledCourses();
        System.out.print(Messages.get("student.courses.drop_id"));
        String id = scanner.nextLine().trim();
        enrolledCourses.stream()
                .filter(c -> c.getCourseId().equals(id))
                .findFirst()
                .ifPresentOrElse(this::dropCourse, () -> System.out.println(Messages.get("student.courses.not_enrolled")));
    }

    private void submitRequestInteractive() {
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

    private void borrowBookInteractive() {
        System.out.println(Messages.get("student.book.available"));
        Database.getInstance().getBooks().stream()
                .filter(b -> !b.isBorrowed())
                .forEach(System.out::println);
        System.out.print(Messages.get("student.book.title_prompt"));
        String title = scanner.nextLine().trim();
        Database.getInstance().findBookByTitle(title).ifPresentOrElse(
                this::borrowBook,
                () -> System.out.println(Messages.get("student.book.not_available")));
    }

    private void returnBookInteractive() {
        System.out.println(Messages.get("student.book.your_books"));
        borrowedBooks.forEach(System.out::println);
        System.out.print(Messages.get("student.book.return_prompt"));
        String title = scanner.nextLine().trim();
        borrowedBooks.stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(this::returnBook, () -> System.out.println(Messages.get("student.book.not_found")));
    }

    public DegreeType getDegreeType() { return degreeType; }
    public void setDegreeType(DegreeType degreeType) { this.degreeType = degreeType; }
    public int getStudyYear() { return studyYear; }
    public void setStudyYear(int studyYear) { this.studyYear = studyYear; }
    public List<Course> getEnrolledCourses() { return Collections.unmodifiableList(enrolledCourses); }
    public int getAvailableCredits() { return availableCredits; }
    public int getFailCount() { return failCount; }
}
