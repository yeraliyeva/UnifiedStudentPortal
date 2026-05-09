package application.usecase.course;

import application.Result;
import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.repository.UserRepository;
import domain.user.Student;
import infrastructure.logging.Logger;

public final class DropCourse {
    private final CourseRepository courses;
    private final UserRepository users;
    private final Logger logger;

    public DropCourse(CourseRepository courses, UserRepository users, Logger logger) {
        this.courses = courses;
        this.users = users;
        this.logger = logger;
    }

    public Result execute(Student student, CourseId courseId) {
        Course course = courses.findById(courseId).orElse(null);
        if (course == null) return Result.fail("Course not found.");
        if (!student.enrolledCourses().contains(courseId)) return Result.fail("Not enrolled in that course.");

        student.recordDrop(courseId, course.credits());
        course.unenroll(student.username());
        users.save(student);
        courses.save(course);
        logger.log(student.username(), "Dropped course: " + course.name());
        return Result.ok("Dropped: " + course.name());
    }
}
