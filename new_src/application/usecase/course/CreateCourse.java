package application.usecase.course;

import application.Result;
import domain.course.Capacity;
import domain.course.Course;
import domain.course.CourseId;
import domain.enums.DisciplineType;
import domain.repository.CourseRepository;
import domain.shared.Credits;
import domain.shared.IdSequence;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class CreateCourse {
    private final CourseRepository courses;
    private final IdSequence courseIds;
    private final Logger logger;

    public CreateCourse(CourseRepository courses, IdSequence courseIds, Logger logger) {
        this.courses = courses;
        this.courseIds = courseIds;
        this.logger = logger;
    }

    public Course execute(Username actor, String name, int credits, DisciplineType type, int maxStudents) {
        Course course = new Course(CourseId.of(courseIds.next()), name,
                new Credits(credits), type, new Capacity(maxStudents));
        courses.save(course);
        logger.log(actor, "Created course: " + name);
        return course;
    }
}
