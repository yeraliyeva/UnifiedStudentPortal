package domain.rules;

import domain.course.Course;
import domain.course.CourseId;
import domain.course.Lesson;
import domain.repository.CourseRepository;
import domain.user.Student;

public final class ScheduleConflictRule implements EnrollmentRule {
    private final CourseRepository courses;

    public ScheduleConflictRule(CourseRepository courses) { this.courses = courses; }

    @Override public EnrollmentDecision check(Student student, Course newCourse) {
        for (CourseId enrolledId : student.enrolledCourses()) {
            Course enrolled = courses.findById(enrolledId).orElse(null);
            if (enrolled == null) continue;
            for (Lesson taken : enrolled.lessons()) {
                for (Lesson incoming : newCourse.lessons()) {
                    if (taken.slot().overlaps(incoming.slot())) {
                        return EnrollmentDecision.deny("Schedule conflict with " + enrolled.name()
                                + " on " + taken.slot());
                    }
                }
            }
        }
        return EnrollmentDecision.allow();
    }
}
