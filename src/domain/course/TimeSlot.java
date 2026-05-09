package domain.course;

import domain.enums.WeekDay;

public record TimeSlot(WeekDay day, String time) {
    public TimeSlot {
        if (day == null) throw new IllegalArgumentException("day");
        if (time == null || time.isBlank()) throw new IllegalArgumentException("time");
    }

    public boolean overlaps(TimeSlot other) {
        return this.day == other.day && this.time.equals(other.time);
    }

    @Override public String toString() { return day + " " + time; }
}
