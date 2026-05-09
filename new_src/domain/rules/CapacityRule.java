package domain.rules;

import domain.course.Course;
import domain.user.Student;

public final class CapacityRule implements EnrollmentRule {
    @Override public EnrollmentDecision check(Student student, Course course) {
        if (course.isFull()) {
            return EnrollmentDecision.deny("Course is full (" + course.capacity().max() + " seats taken).");
        }
        return EnrollmentDecision.allow();
    }
}
