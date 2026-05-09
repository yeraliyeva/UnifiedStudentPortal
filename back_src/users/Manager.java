package users;

import common.LogManager;
import common.Messages;
import communication.News;
import communication.Request;
import data.Database;
import education.Course;
import education.Lesson;
import enums.*;
import interfaces.Managable;

import java.time.LocalDate;

public class Manager extends Employee implements Managable {

    private ManagerPosition position;

    public Manager(String firstName, String lastName, String username,
                   String password, Gender gender, LocalDate dateOfBirth,
                   String email, Faculty faculty, double salary,
                   LocalDate hireDate, String insuranceNumber,
                   ManagerPosition position) {
        super(firstName, lastName, username, password, gender, dateOfBirth,
              email, faculty, salary, hireDate, insuranceNumber);
        this.position = position;
    }

    public void viewRequests() {
        System.out.println(Messages.fmt("manager.requests.title", position));
        Database.getInstance().getAllRequests().stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING && matchesPosition(r))
                .forEach(System.out::println);
    }

    private boolean matchesPosition(Request r) {
        return switch (position) {
            case OR -> r.getType() == HelpType.TRANSCRIPT_FOR_SEMESTER
                    || r.getType() == HelpType.TRANSCRIPT_FOR_YEAR
                    || r.getType() == HelpType.TRANSCRIPT_FOR_ENTIRE_STUDY
                    || r.getType() == HelpType.CERTIFICATE_OF_EDUCATION
                    || r.getType() == HelpType.RESTORING_ONAY_CARD;
            case DEPARTMENT -> r.getType() == HelpType.COORDINATION_OF_DIPLOMA_TOPIC
                    || r.getType() == HelpType.WORKAROUND_SHEET
                    || r.getType() == HelpType.ACADEMIC_MOBILITY;
            case DEANS_OFFICE -> r.getType() == HelpType.HELP_DEPARTMENT_OF_DEFENSE
                    || r.getType() == HelpType.HELP_LARGE_FAMILIES
                    || r.getType() == HelpType.HELP_LOSS_OF_BREADWINNER
                    || r.getType() == HelpType.HELP_FINANCING_KAZENERGY
                    || r.getType() == HelpType.INFORMATION_ABOUT_PLACE_OF_REQUIREMENT
                    || r.getType() == HelpType.REQUEST_FOR_CREATING_ORGANIZATION;
        };
    }

    public void processRequestInteractive() {
        viewRequests();
        System.out.print(Messages.get("manager.request.enter_id"));
        int id = readInt();
        Database.getInstance().findRequestById(id).ifPresentOrElse(req -> {
            System.out.println(Messages.get("manager.request.action"));
            int choice = readInt();
            if (choice == 1) {
                req.setStatus(RequestStatus.ACCEPTED);
                LogManager.getInstance().log(getUsername(), "Accepted request #" + id);
                System.out.println(Messages.get("manager.request.accepted"));
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Accepted",
                        "Your request has been accepted by Manager " + getUsername() + ".",
                        UrgencyLevel.MEDIUM);
            } else if (choice == 2) {
                req.setStatus(RequestStatus.NOT_APPROVED);
                LogManager.getInstance().log(getUsername(), "Rejected request #" + id);
                System.out.println(Messages.get("manager.request.rejected"));
                sendMessage(req.getRequesterUsername(),
                        "Request #" + req.getId() + " Rejected",
                        "Your request was rejected by Manager " + getUsername() + ".",
                        UrgencyLevel.MEDIUM);
            }
        }, () -> System.out.println(Messages.get("manager.request.not_found")));
    }

    public void createCourseInteractive() {
        System.out.print(Messages.get("manager.course.name"));
        String name = scanner.nextLine().trim();
        System.out.print(Messages.get("manager.course.credits"));
        int credits = readInt();
        System.out.println(Messages.get("manager.course.type"));
        DisciplineType type = switch (scanner.nextLine().trim().toUpperCase()) {
            case "MINOR" -> DisciplineType.MINOR;
            case "FREE"  -> DisciplineType.FREE;
            default      -> DisciplineType.MAJOR;
        };
        Course course = new Course(name, credits, type);
        Database.getInstance().addCourse(course);
        LogManager.getInstance().log(getUsername(), "Created course: " + name);
        System.out.println(Messages.fmt("manager.course.created", course));
    }

    public void assignTeacherToCourseInteractive() {
        System.out.println(Messages.get("manager.course.courses"));
        Database.getInstance().getCourses().forEach(System.out::println);
        System.out.print(Messages.get("manager.course.enter_id"));
        String cid = scanner.nextLine().trim();
        System.out.println(Messages.get("manager.course.teachers"));
        Database.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Teacher)
                .forEach(System.out::println);
        System.out.print(Messages.get("manager.course.teacher_user"));
        String tUsername = scanner.nextLine().trim();
        Database.getInstance().findCourseById(cid).ifPresentOrElse(course -> {
            User u = Database.getInstance().getUser(tUsername);
            if (u instanceof Teacher teacher) {
                teacher.assignToCourse(course);
                LogManager.getInstance().log(getUsername(), "Assigned " + tUsername + " to " + course.getCourseName());
                System.out.println(Messages.get("manager.course.assigned"));
            } else {
                System.out.println(Messages.get("manager.course.not_teacher"));
            }
        }, () -> System.out.println(Messages.get("manager.course.not_found")));
    }

    public void addLessonToCourseInteractive() {
        System.out.println(Messages.get("manager.course.courses"));
        Database.getInstance().getCourses().forEach(System.out::println);
        System.out.print(Messages.get("manager.course.enter_id"));
        String cid = scanner.nextLine().trim();
        Database.getInstance().findCourseById(cid).ifPresentOrElse(course -> {
            System.out.print(Messages.get("manager.lesson.type"));
            LessonType ltype = switch (scanner.nextLine().trim().toUpperCase()) {
                case "PRACTICE"     -> LessonType.PRACTICE;
                case "OFFICE_HOURS" -> LessonType.OFFICE_HOURS;
                case "EXAM"         -> LessonType.EXAM;
                default             -> LessonType.LECTURE;
            };
            System.out.print(Messages.get("manager.lesson.day"));
            WeekDay day;
            try { day = WeekDay.valueOf(scanner.nextLine().trim().toUpperCase()); }
            catch (IllegalArgumentException e) { day = WeekDay.MONDAY; }
            System.out.print(Messages.get("manager.lesson.time"));
            String time = scanner.nextLine().trim();
            System.out.print(Messages.get("manager.lesson.room"));
            String room = scanner.nextLine().trim();
            if (!isRoomAvailable(room, day, time)) {
                System.out.println(Messages.fmt("manager.lesson.room_busy", room, day, time));
                LogManager.getInstance().log(getUsername(),
                        "Room conflict prevented: " + room + " " + day + " " + time);
                return;
            }
            Lesson lesson = new Lesson(ltype, day, time, room);
            course.addLesson(lesson);
            LogManager.getInstance().log(getUsername(), "Added lesson to " + course.getCourseName());
            System.out.println(Messages.fmt("manager.lesson.added", lesson));
        }, () -> System.out.println(Messages.get("manager.course.not_found")));
    }

    public boolean isRoomAvailable(String room, WeekDay day, String time) {
        for (Course c : Database.getInstance().getCourses()) {
            for (Lesson l : c.getLessons()) {
                if (l.getRoom().equalsIgnoreCase(room)
                        && l.getDay() == day
                        && l.getTime().equals(time)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void viewExamScheduleAll() {
        System.out.println(Messages.get("manager.exams.title"));
        boolean any = false;
        for (Course c : Database.getInstance().getCourses()) {
            for (Lesson l : c.getLessons()) {
                if (l.getType() == LessonType.EXAM) {
                    System.out.println("  " + c.getCourseName() + " — " + l);
                    any = true;
                }
            }
        }
        if (!any) System.out.println(Messages.get("manager.exams.empty"));
    }

    public void setCoursePrerequisiteInteractive() {
        Database.getInstance().getCourses().forEach(System.out::println);
        System.out.print(Messages.get("manager.prereq.target"));
        String tid = scanner.nextLine().trim();
        System.out.print(Messages.get("manager.prereq.required"));
        String pid = scanner.nextLine().trim();
        Database.getInstance().findCourseById(tid).ifPresentOrElse(target ->
                Database.getInstance().findCourseById(pid).ifPresentOrElse(prereq -> {
                    target.addPrerequisite(prereq);
                    LogManager.getInstance().log(getUsername(),
                            "Set prereq " + prereq.getCourseName() + " for " + target.getCourseName());
                    System.out.println(Messages.fmt("manager.prereq.added",
                            prereq.getCourseName(), target.getCourseName()));
                }, () -> System.out.println(Messages.get("manager.course.not_found"))),
                () -> System.out.println(Messages.get("manager.course.not_found")));
    }

    public void setCourseCapacityInteractive() {
        Database.getInstance().getCourses().forEach(System.out::println);
        System.out.print(Messages.get("manager.capacity.course"));
        String cid = scanner.nextLine().trim();
        Database.getInstance().findCourseById(cid).ifPresentOrElse(c -> {
            System.out.print(Messages.get("manager.capacity.value"));
            int cap = readInt();
            if (cap < 1) { System.out.println(Messages.get("manager.capacity.invalid")); return; }
            c.setMaxStudents(cap);
            LogManager.getInstance().log(getUsername(),
                    "Set capacity " + cap + " for " + c.getCourseName());
            System.out.println(Messages.fmt("manager.capacity.set", c.getCourseName(), cap));
        }, () -> System.out.println(Messages.get("manager.course.not_found")));
    }

    public void generateAcademicReport() {
        System.out.println(Messages.get("manager.report.header"));
        System.out.println(Messages.fmt("manager.report.faculty", getFaculty()));
        long studentCount = Database.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Student).count();
        long teacherCount = Database.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Teacher).count();
        System.out.println(Messages.fmt("manager.report.totals",
                Database.getInstance().getCourses().size(), studentCount, teacherCount));
        System.out.println(Messages.get("manager.report.courses_section"));
        for (Course c : Database.getInstance().getCourses()) {
            double avg = c.getAllGrades().values().stream()
                    .mapToInt(g -> g.getTotal()).average().orElse(0);
            long passing = c.getAllGrades().values().stream()
                    .filter(g -> g.getTotal() >= 50).count();
            System.out.println(Messages.fmt("manager.report.course_row",
                    c.getCourseName(),
                    c.getStudents().size(),
                    c.getMaxStudents(),
                    String.format("%.1f", avg),
                    passing));
        }
        System.out.println(Messages.get("manager.report.top_section"));
        Database.getInstance().getStudentsByGPA().stream().limit(5).forEach(s ->
                System.out.printf("  %s %s — GPA %.2f%n",
                        s.getFirstName(), s.getLastName(), s.calculateGPA()));
        LogManager.getInstance().log(getUsername(), "Generated academic report");
    }

    public void addNewsInteractive() {
        System.out.print(Messages.get("manager.news.title_prompt"));
        String title = scanner.nextLine().trim();
        System.out.print(Messages.get("manager.news.body_prompt"));
        String body = scanner.nextLine().trim();
        News news = new News(title, body, getUsername());
        Database.getInstance().addNews(news);
        LogManager.getInstance().log(getUsername(), "Added news: " + title);
        System.out.println(Messages.fmt("manager.news.published", news));
    }

    public void editNewsInteractive() {
        viewNews();
        System.out.print(Messages.get("manager.news.edit_id"));
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            System.out.print(Messages.get("manager.news.new_title"));
            String title = scanner.nextLine().trim();
            System.out.print(Messages.get("manager.news.new_body"));
            String body = scanner.nextLine().trim();
            news.edit(title, body);
            LogManager.getInstance().log(getUsername(), "Edited news #" + id);
            System.out.println(Messages.get("manager.news.updated"));
        }, () -> System.out.println(Messages.get("manager.news.not_found")));
    }

    public void deleteNewsInteractive() {
        viewNews();
        System.out.print(Messages.get("manager.news.delete_id"));
        int id = readInt();
        Database.getInstance().findNewsById(id).ifPresentOrElse(news -> {
            Database.getInstance().removeNews(news);
            LogManager.getInstance().log(getUsername(), "Deleted news #" + id);
            System.out.println(Messages.get("manager.news.deleted"));
        }, () -> System.out.println(Messages.get("manager.news.not_found")));
    }

    @Override
    public void viewAcademicStatistics() {
        System.out.println(Messages.get("manager.stats.title"));
        System.out.println(Messages.fmt("manager.stats.courses", Database.getInstance().getCourses().size()));
        System.out.println(Messages.fmt("manager.stats.students", Database.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Student).count()));
        for (Course c : Database.getInstance().getCourses()) {
            double avg = c.getAllGrades().values().stream()
                    .mapToInt(g -> g.getTotal()).average().orElse(0);
            System.out.println(Messages.fmt("manager.stats.course_info",
                    c.getCourseName(), c.getStudents().size(), String.format("%.1f", avg)));
        }
    }

    public void viewStudentsByGPA() {
        System.out.println(Messages.get("manager.students.gpa_title"));
        Database.getInstance().getStudentsByGPA().forEach(s ->
                System.out.printf("  %s %s (%s) \u2014 GPA: %.2f%n",
                        s.getFirstName(), s.getLastName(), s.getUsername(), s.calculateGPA()));
    }

    public void viewStudentsAlphabetically() {
        System.out.println(Messages.get("manager.students.alpha_title"));
        Database.getInstance().getStudentsAlphabetically().forEach(s ->
                System.out.println("  " + s.getLastName() + ", " + s.getFirstName()
                        + " (" + s.getUsername() + ") \u2014 Year: " + s.getStudyYear()));
    }

    @Override
    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println(Messages.get("manager.menu.title"));
            System.out.println("1. " + Messages.get("manager.menu.1"));
            System.out.println("2. " + Messages.get("manager.menu.2"));
            System.out.println("3. " + Messages.get("manager.menu.3"));
            System.out.println("4. " + Messages.get("manager.menu.4"));
            System.out.println("5. " + Messages.get("manager.menu.5"));
            System.out.println("6. " + Messages.get("manager.menu.6"));
            System.out.println("7. " + Messages.get("manager.menu.7"));
            System.out.println("8. " + Messages.get("manager.menu.8"));
            System.out.println("9. " + Messages.get("manager.menu.9"));
            System.out.println("10. " + Messages.get("manager.menu.10"));
            System.out.println("11. " + Messages.get("manager.menu.11"));
            System.out.println("12. " + Messages.get("manager.menu.12"));
            System.out.println("13. " + Messages.get("manager.menu.13"));
            System.out.println("14. " + Messages.get("manager.menu.14"));
            System.out.println("15. " + Messages.get("manager.menu.15"));
            System.out.println("16. " + Messages.get("manager.menu.16"));
            System.out.println("17. " + Messages.get("manager.menu.17"));
            System.out.println("18. " + Messages.get("manager.menu.18"));
            System.out.println("19. " + Messages.get("manager.menu.19"));
            System.out.println("20. " + Messages.get("manager.menu.20"));
            System.out.println("0. " + Messages.get("common.logout"));
            System.out.print(Messages.get("common.prompt"));

            switch (readInt()) {
                case 1  -> viewRequests();
                case 2  -> processRequestInteractive();
                case 3  -> createCourseInteractive();
                case 4  -> assignTeacherToCourseInteractive();
                case 5  -> addLessonToCourseInteractive();
                case 6  -> viewAcademicStatistics();
                case 7  -> viewStudentsByGPA();
                case 8  -> viewStudentsAlphabetically();
                case 9  -> addNewsInteractive();
                case 10 -> editNewsInteractive();
                case 11 -> deleteNewsInteractive();
                case 12 -> viewNews();
                case 13 -> viewInbox();
                case 14 -> sendMessageInteractive();
                case 15 -> { viewPersonalInfo(); editPersonalInfo(); }
                case 16 -> { System.out.print(Messages.get("employee.order.desc")); addOrder(scanner.nextLine().trim()); }
                case 17 -> setCoursePrerequisiteInteractive();
                case 18 -> setCourseCapacityInteractive();
                case 19 -> viewExamScheduleAll();
                case 20 -> generateAcademicReport();
                case 0, -2 -> running = false;
                default -> System.out.println(Messages.get("common.invalid"));
            }
        }
    }

    public ManagerPosition getPosition() { return position; }
    public void setPosition(ManagerPosition p) { this.position = p; }
}
