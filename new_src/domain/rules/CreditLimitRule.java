package domain.rules;

import domain.course.Course;
import domain.user.Student;

public final class CreditLimitRule implements EnrollmentRule {
    @Override public EnrollmentDecision check(Student student, Course course) {
        if (!student.availableCredits().covers(course.credits())) {
            return EnrollmentDecision.deny("Not enough credits. Available: " + student.availableCredits()
                    + ", Required: " + course.credits());
        }
        return EnrollmentDecision.allow();
    }
}
