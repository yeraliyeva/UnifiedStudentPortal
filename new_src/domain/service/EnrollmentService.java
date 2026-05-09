package domain.service;

import domain.course.Course;
import domain.rules.EnrollmentDecision;
import domain.rules.EnrollmentRule;
import domain.user.Student;

import java.util.List;

public final class EnrollmentService {
    private final List<EnrollmentRule> rules;

    public EnrollmentService(List<EnrollmentRule> rules) { this.rules = List.copyOf(rules); }

    public EnrollmentDecision tryEnroll(Student student, Course course) {
        for (EnrollmentRule rule : rules) {
            EnrollmentDecision decision = rule.check(student, course);
            if (!decision.allowed()) return decision;
        }
        return EnrollmentDecision.allow();
    }
}
