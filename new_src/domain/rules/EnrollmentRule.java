package domain.rules;

import domain.course.Course;
import domain.user.Student;

public interface EnrollmentRule {
    EnrollmentDecision check(Student student, Course course);
}
