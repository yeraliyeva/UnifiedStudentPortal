package domain.service;

import domain.course.Course;
import domain.course.CourseId;
import domain.course.Grade;
import domain.repository.CourseRepository;
import domain.user.Student;

public final class GpaCalculator {
    private final CourseRepository courses;

    public GpaCalculator(CourseRepository courses) { this.courses = courses; }

    public double of(Student student) {
        int count = 0;
        double total = 0;
        for (CourseId id : student.enrolledCourses()) {
            Course c = courses.findById(id).orElse(null);
            if (c == null) continue;
            Grade g = c.gradeOf(student.username()).orElse(null);
            if (g != null) {
                total += g.total();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }
}
