package application.usecase.course;

import application.Result;
import domain.course.Course;
import domain.course.CourseId;
import domain.course.Lesson;
import domain.course.Room;
import domain.course.TimeSlot;
import domain.enums.LessonType;
import domain.repository.CourseRepository;
import domain.service.RoomScheduler;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class AddLesson {
    private final CourseRepository courses;
    private final RoomScheduler scheduler;
    private final Logger logger;

    public AddLesson(CourseRepository courses, RoomScheduler scheduler, Logger logger) {
        this.courses = courses;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    public Result execute(Username actor, CourseId courseId, LessonType type, TimeSlot slot, Room room) {
        Course course = courses.findById(courseId).orElse(null);
        if (course == null) return Result.fail("Course not found.");

        if (!scheduler.isAvailable(room, slot)) {
            return Result.fail("Room " + room + " is already booked at " + slot);
        }

        course.addLesson(new Lesson(type, slot, room));
        courses.save(course);
        logger.log(actor, "Added " + type + " on " + slot + " in " + room + " to " + course.name());
        return Result.ok("Lesson added.");
    }
}
