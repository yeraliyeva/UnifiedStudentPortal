package domain.service;

import domain.course.Course;
import domain.course.Lesson;
import domain.course.Room;
import domain.course.TimeSlot;
import domain.repository.CourseRepository;

public final class RoomScheduler {
    private final CourseRepository courses;

    public RoomScheduler(CourseRepository courses) { this.courses = courses; }

    public boolean isAvailable(Room room, TimeSlot slot) {
        for (Course c : courses.findAll()) {
            for (Lesson l : c.lessons()) {
                if (l.room().sameAs(room) && l.slot().overlaps(slot)) return false;
            }
        }
        return true;
    }
}
