package domain.course;

import domain.enums.LessonType;

public record Lesson(LessonType type, TimeSlot slot, Room room) {
    public Lesson {
        if (type == null || slot == null || room == null) throw new IllegalArgumentException("lesson fields");
    }

    public boolean isExam() { return type == LessonType.EXAM; }

    @Override public String toString() {
        return type + " | " + slot + " | Room: " + room;
    }
}
