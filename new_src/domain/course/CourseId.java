package domain.course;

public record CourseId(String value) {
    public CourseId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("courseId");
    }
    public static CourseId of(int n) { return new CourseId("CRS-" + n); }
    @Override public String toString() { return value; }
}
