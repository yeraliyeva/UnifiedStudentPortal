package domain.rules;

import domain.course.Course;
import domain.user.Student;

public final class AlreadyEnrolledRule implements EnrollmentRule {
    @Override public EnrollmentDecision check(Student student, Course course) {
        if (student.enrolledCourses().contains(course.id())) {
            return EnrollmentDecision.deny("Already enrolled in " + course.name());
        }
        return EnrollmentDecision.allow();
    }
}
