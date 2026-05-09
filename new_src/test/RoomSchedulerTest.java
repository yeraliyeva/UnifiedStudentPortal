package test;

import domain.course.Course;
import domain.course.Lesson;
import domain.course.Room;
import domain.course.TimeSlot;
import domain.enums.LessonType;
import domain.enums.WeekDay;
import domain.repository.CourseRepository;
import domain.service.RoomScheduler;
import infrastructure.persistence.inmemory.InMemoryCourseRepository;

public final class RoomSchedulerTest {

    public static void runAll() {
        TestRunner.run("RoomScheduler: same room/day/time -> not available", RoomSchedulerTest::testCollision);
        TestRunner.run("RoomScheduler: different day or time -> available", RoomSchedulerTest::testNoCollision);
    }

    private static void testCollision() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course c = Fixtures.course("A", 5, 30, 1);
        c.addLesson(new Lesson(LessonType.LECTURE, new TimeSlot(WeekDay.MONDAY, "09:00"), new Room("R1")));
        repo.save(c);
        boolean free = new RoomScheduler(repo).isAvailable(new Room("R1"), new TimeSlot(WeekDay.MONDAY, "09:00"));
        Assert.isFalse(free, "Should detect collision");
    }

    private static void testNoCollision() {
        CourseRepository repo = new InMemoryCourseRepository();
        Course c = Fixtures.course("A", 5, 30, 1);
        c.addLesson(new Lesson(LessonType.LECTURE, new TimeSlot(WeekDay.MONDAY, "09:00"), new Room("R1")));
        repo.save(c);
        RoomScheduler scheduler = new RoomScheduler(repo);
        Assert.isTrue(scheduler.isAvailable(new Room("R1"), new TimeSlot(WeekDay.TUESDAY, "09:00")), "Different day -> free");
        Assert.isTrue(scheduler.isAvailable(new Room("R1"), new TimeSlot(WeekDay.MONDAY, "10:30")), "Different time -> free");
        Assert.isTrue(scheduler.isAvailable(new Room("R2"), new TimeSlot(WeekDay.MONDAY, "09:00")), "Different room -> free");
    }
}
