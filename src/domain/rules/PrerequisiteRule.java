package domain.rules;

import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import domain.user.Student;

public final class PrerequisiteRule implements EnrollmentRule {
    private final CourseRepository courses;

    public PrerequisiteRule(CourseRepository courses) { this.courses = courses; }

    @Override public EnrollmentDecision check(Student student, Course course) {
        for (CourseId prereqId : course.prerequisites()) {
            if (!student.completedCourses().contains(prereqId)) {
                String name = courses.findById(prereqId).map(Course::name).orElse(prereqId.value());
                return EnrollmentDecision.deny("Missing prerequisite: " + name);
            }
        }
        return EnrollmentDecision.allow();
    }
}
