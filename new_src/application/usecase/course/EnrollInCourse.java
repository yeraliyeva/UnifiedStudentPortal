package application.usecase.course;

import application.Result;
import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.repository.UserRepository;
import domain.rules.EnrollmentDecision;
import domain.service.EnrollmentService;
import domain.user.Student;
import infrastructure.logging.Logger;

public final class EnrollInCourse {
    private final EnrollmentService enrollment;
    private final CourseRepository courses;
    private final UserRepository users;
    private final Logger logger;

    public EnrollInCourse(EnrollmentService enrollment, CourseRepository courses, UserRepository users, Logger logger) {
        this.enrollment = enrollment;
        this.courses = courses;
        this.users = users;
        this.logger = logger;
    }

    public Result execute(Student student, CourseId courseId) {
        Course course = courses.findById(courseId).orElse(null);
        if (course == null) return Result.fail("Course not found.");

        EnrollmentDecision decision = enrollment.tryEnroll(student, course);
        if (!decision.allowed()) {
            logger.log(student.username(), "Enrollment denied: " + decision.reason());
            return Result.fail(decision.reason());
        }

        student.recordEnrollment(course.id(), course.credits());
        course.enroll(student.username());
        users.save(student);
        courses.save(course);
        logger.log(student.username(), "Enrolled in course: " + course.name());
        return Result.ok("Enrolled in: " + course.name());
    }
}
