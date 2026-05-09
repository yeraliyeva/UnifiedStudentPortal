package application.usecase.course;

import application.Result;
import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class SetCoursePrerequisite {
    private final CourseRepository courses;
    private final Logger logger;

    public SetCoursePrerequisite(CourseRepository courses, Logger logger) {
        this.courses = courses;
        this.logger = logger;
    }

    public Result execute(Username actor, CourseId target, CourseId prereq) {
        Course t = courses.findById(target).orElse(null);
        Course p = courses.findById(prereq).orElse(null);
        if (t == null || p == null) return Result.fail("Course not found.");
        t.addPrerequisite(prereq);
        courses.save(t);
        logger.log(actor, "Added prereq " + p.name() + " to " + t.name());
        return Result.ok("Prerequisite added.");
    }
}
