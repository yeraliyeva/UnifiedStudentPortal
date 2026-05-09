package application.usecase.course;

import application.Result;
import domain.course.Capacity;
import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class SetCourseCapacity {
    private final CourseRepository courses;
    private final Logger logger;

    public SetCourseCapacity(CourseRepository courses, Logger logger) {
        this.courses = courses;
        this.logger = logger;
    }

    public Result execute(Username actor, CourseId courseId, int maxStudents) {
        Course course = courses.findById(courseId).orElse(null);
        if (course == null) return Result.fail("Course not found.");
        course.changeCapacity(new Capacity(maxStudents));
        courses.save(course);
        logger.log(actor, "Set capacity " + maxStudents + " for " + course.name());
        return Result.ok("Capacity updated.");
    }
}
