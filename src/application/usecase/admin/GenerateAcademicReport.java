package application.usecase.admin;

import domain.course.Course;
import domain.repository.CourseRepository;
import domain.repository.UserRepository;
import domain.service.GpaCalculator;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import infrastructure.logging.Logger;

import java.util.Comparator;
import java.util.List;

public final class GenerateAcademicReport {
    public record Row(String courseName, int enrolled, int max, double avg, long passing) {}
    public record TopStudent(String username, String fullName, double gpa) {}
    public record Report(int totalCourses, long totalStudents, long totalTeachers,
                         double averageGpa, long failingStudents,
                         List<Row> rows, List<TopStudent> top) {}

    private final CourseRepository courses;
    private final UserRepository users;
    private final GpaCalculator gpa;
    private final Logger logger;

    public GenerateAcademicReport(CourseRepository courses, UserRepository users, GpaCalculator gpa, Logger logger) {
        this.courses = courses;
        this.users = users;
        this.gpa = gpa;
        this.logger = logger;
    }

    public Report execute(domain.shared.Username actor) {
        List<User> all = users.findAll().stream().toList();
        List<Student> students = all.stream()
                .filter(u -> u instanceof Student).map(u -> (Student) u).toList();
        long teacherCount = all.stream().filter(u -> u instanceof Teacher).count();
        List<Row> rows = courses.findAll().stream().map(this::rowOf).toList();
        List<TopStudent> top = students.stream()
                .map(s -> new TopStudent(s.username().value(), s.name().full(), gpa.of(s)))
                .sorted(Comparator.comparingDouble(TopStudent::gpa).reversed())
                .limit(5)
                .toList();
        double averageGpa = students.stream().mapToDouble(gpa::of).average().orElse(0.0);
        long failingStudents = students.stream().filter(s -> s.failCount() > 0).count();
        logger.log(actor, "Generated academic report");
        return new Report(courses.findAll().size(), students.size(), teacherCount,
                averageGpa, failingStudents, rows, top);
    }

    private Row rowOf(Course c) {
        double avg = c.allGrades().values().stream().mapToInt(g -> g.total()).average().orElse(0);
        long passing = c.allGrades().values().stream().filter(g -> g.isPassing()).count();
        return new Row(c.name(), c.students().size(), c.capacity().max(), avg, passing);
    }
}
