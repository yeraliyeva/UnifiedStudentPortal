package education;

import enums.Faculty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Academic specialty / program within a faculty.
 */
public class Specialty {
    private final String specialtyId;
    private String name;
    private Faculty faculty;
    private final List<Course> courses = new ArrayList<>();

    public Specialty(String specialtyId, String name, Faculty faculty) {
        this.specialtyId = specialtyId;
        this.name = name;
        this.faculty = faculty;
    }

    public void addCourse(Course course) {
        if (!courses.contains(course)) courses.add(course);
    }
    public void removeCourse(Course course) { courses.remove(course); }
    public List<Course> getCourses() { return Collections.unmodifiableList(courses); }

    public String getSpecialtyId() { return specialtyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    @Override
    public String toString() {
        return "[" + specialtyId + "] " + name + " (" + faculty + ")";
    }
}
