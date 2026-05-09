package education;

import enums.DisciplineType;
import users.Student;
import users.Teacher;

import java.util.*;

/**
 * Represents a university course.
 *
 * Grades stored here per student (Map<studentUsername, AttestationResult>).
 * Multiple teachers allowed per course.
 */
public class Course {
    private static int idCounter = 1;

    private final String courseId;
    private String courseName;
    private int credits;
    private DisciplineType type;
    private int maxStudents = 1000;

    private final List<Teacher> teachers = new ArrayList<>();
    private final List<Student> students = new ArrayList<>();
    private final List<Lesson> lessons = new ArrayList<>();
    private final List<Course> prerequisites = new ArrayList<>();

    // studentUsername -> attestation result
    private final Map<String, AttestationResult> grades = new HashMap<>();

    public Course(String courseName, int credits, DisciplineType type) {
        this.courseId = "CRS-" + idCounter++;
        this.courseName = courseName;
        this.credits = credits;
        this.type = type;
    }

    public Course(String courseName, int credits, DisciplineType type, int maxStudents) {
        this(courseName, credits, type);
        this.maxStudents = maxStudents;
    }

    // ── teachers ──────────────────────────────────────────────────
    public void addTeacher(Teacher teacher) {
        if (!teachers.contains(teacher)) teachers.add(teacher);
    }
    public List<Teacher> getTeachers() { return Collections.unmodifiableList(teachers); }

    // ── students ──────────────────────────────────────────────────
    public void enrollStudent(Student student) {
        if (!students.contains(student)) {
            students.add(student);
            grades.put(student.getUsername(), new AttestationResult(0, 0, 0));
        }
    }
    public void removeStudent(Student student) {
        students.remove(student);
        grades.remove(student.getUsername());
    }
    public List<Student> getStudents() { return Collections.unmodifiableList(students); }
    public boolean hasStudent(Student s) { return students.contains(s); }

    // ── lessons ───────────────────────────────────────────────────
    public void addLesson(Lesson lesson) { lessons.add(lesson); }
    public List<Lesson> getLessons() { return Collections.unmodifiableList(lessons); }

    // ── prerequisites ─────────────────────────────────────────────
    public void addPrerequisite(Course prereq) {
        if (prereq != null && !prereq.equals(this) && !prerequisites.contains(prereq)) {
            prerequisites.add(prereq);
        }
    }
    public List<Course> getPrerequisites() { return Collections.unmodifiableList(prerequisites); }

    // ── capacity ──────────────────────────────────────────────────
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    public boolean isFull() { return students.size() >= maxStudents; }
    public int getRemainingSeats() { return Math.max(0, maxStudents - students.size()); }

    // ── grades ────────────────────────────────────────────────────
    public void setGrade(String studentUsername, int att1, int att2, int exam) {
        AttestationResult r = grades.get(studentUsername);
        if (r != null) {
            r.setFirstHalf(att1);
            r.setSecondHalf(att2);
            r.setExam(exam);
        }
    }
    public AttestationResult getGrade(String studentUsername) {
        return grades.getOrDefault(studentUsername, null);
    }
    public Map<String, AttestationResult> getAllGrades() { return Collections.unmodifiableMap(grades); }

    // ── getters / setters ─────────────────────────────────────────
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public DisciplineType getType() { return type; }
    public void setType(DisciplineType type) { this.type = type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;
        return Objects.equals(courseId, course.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @Override
    public String toString() {
        return "[" + courseId + "] " + courseName + " (" + credits + " credits, " + type + ")";
    }
}
