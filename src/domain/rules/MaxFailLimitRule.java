package domain.rules;

import domain.course.Course;
import domain.user.Student;

public final class MaxFailLimitRule implements EnrollmentRule {
    @Override public EnrollmentDecision check(Student student, Course course) {
        if (student.hasReachedFailLimit()) {
            return EnrollmentDecision.deny("You have reached the maximum fail limit (" + Student.MAX_FAILS + ").");
        }
        return EnrollmentDecision.allow();
    }
}
