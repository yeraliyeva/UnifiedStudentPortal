package education;

import enums.LessonType;
import enums.WeekDay;

/**
 * A single lesson slot in a course schedule.
 * Marks (attendance/grade) are stored per student username.
 */
public class Lesson {
    private final LessonType type;
    private final WeekDay day;
    private final String time;   // e.g. "09:00"
    private final String room;

    public Lesson(LessonType type, WeekDay day, String time, String room) {
        this.type = type;
        this.day = day;
        this.time = time;
        this.room = room;
    }

    public LessonType getType() { return type; }
    public WeekDay getDay() { return day; }
    public String getTime() { return time; }
    public String getRoom() { return room; }

    @Override
    public String toString() {
        return type + " | " + day + " " + time + " | Room: " + room;
    }
}
