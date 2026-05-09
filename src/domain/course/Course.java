package domain.course;

import domain.enums.DisciplineType;
import domain.shared.Credits;
import domain.shared.Username;

import java.util.*;

public final class Course {
    private final CourseId id;
    private String name;
    private final Credits credits;
    private final DisciplineType type;
    private Capacity capacity;

    private final Set<Username> teacherUsernames = new LinkedHashSet<>();
    private final Set<Username> studentUsernames = new LinkedHashSet<>();
    private final List<Lesson> lessons = new ArrayList<>();
    private final Set<CourseId> prerequisites = new LinkedHashSet<>();
    private final Map<Username, Grade> grades = new HashMap<>();

    public Course(CourseId id, String name, Credits credits, DisciplineType type, Capacity capacity) {
        this.id = id;
        this.name = name;
        this.credits = credits;
        this.type = type;
        this.capacity = capacity;
    }

    public CourseId id() { return id; }
    public String name() { return name; }
    public Credits credits() { return credits; }
    public DisciplineType type() { return type; }
    public Capacity capacity() { return capacity; }
    public void rename(String newName) { this.name = newName; }
    public void changeCapacity(Capacity c) { this.capacity = c; }

    public void enroll(Username student) { studentUsernames.add(student); }
    public void unenroll(Username student) { studentUsernames.remove(student); grades.remove(student); }
    public boolean hasStudent(Username student) { return studentUsernames.contains(student); }
    public Set<Username> students() { return Collections.unmodifiableSet(studentUsernames); }
    public boolean isFull() { return !capacity.canFit(studentUsernames.size()); }
    public int remainingSeats() { return capacity.remaining(studentUsernames.size()); }

    public void assignTeacher(Username teacher) { teacherUsernames.add(teacher); }
    public Set<Username> teachers() { return Collections.unmodifiableSet(teacherUsernames); }

    public void addLesson(Lesson l) { lessons.add(l); }
    public List<Lesson> lessons() { return Collections.unmodifiableList(lessons); }

    public void addPrerequisite(CourseId p) {
        if (!p.equals(id)) prerequisites.add(p);
    }
    public Set<CourseId> prerequisites() { return Collections.unmodifiableSet(prerequisites); }

    public void recordGrade(Username student, Grade grade) {
        if (!studentUsernames.contains(student)) throw new IllegalStateException("not enrolled");
        grades.put(student, grade);
    }
    public Optional<Grade> gradeOf(Username student) { return Optional.ofNullable(grades.get(student)); }
    public Map<Username, Grade> allGrades() { return Collections.unmodifiableMap(grades); }

    @Override public boolean equals(Object o) {
        return o instanceof Course c && id.equals(c.id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() {
        return "[" + id + "] " + name + " (" + credits + " credits, " + type + ", " + studentUsernames.size() + "/" + capacity.max() + ")";
    }
}
