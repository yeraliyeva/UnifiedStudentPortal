package application.usecase.course;

import application.Result;
import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.Teacher;
import domain.user.User;
import infrastructure.logging.Logger;

public final class AssignTeacher {
    private final CourseRepository courses;
    private final UserRepository users;
    private final Logger logger;

    public AssignTeacher(CourseRepository courses, UserRepository users, Logger logger) {
        this.courses = courses;
        this.users = users;
        this.logger = logger;
    }

    public Result execute(Username actor, CourseId courseId, Username teacherUsername) {
        Course course = courses.findById(courseId).orElse(null);
        if (course == null) return Result.fail("Course not found.");

        User u = users.findByUsername(teacherUsername).orElse(null);
        if (!(u instanceof Teacher teacher)) return Result.fail("User is not a teacher.");

        course.assignTeacher(teacher.username());
        teacher.recordCourseAssignment(courseId);
        courses.save(course);
        users.save(teacher);
        logger.log(actor, "Assigned " + teacherUsername + " to " + course.name());
        return Result.ok("Teacher assigned to " + course.name());
    }
}
