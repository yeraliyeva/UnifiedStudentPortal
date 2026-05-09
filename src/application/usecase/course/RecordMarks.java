package application.usecase.course;

import application.Result;
import domain.course.Course;
import domain.course.CourseId;
import domain.course.Grade;
import domain.repository.CourseRepository;
import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import infrastructure.logging.Logger;

public final class RecordMarks {
    private final CourseRepository courses;
    private final UserRepository users;
    private final Logger logger;

    public RecordMarks(CourseRepository courses, UserRepository users, Logger logger) {
        this.courses = courses;
        this.users = users;
        this.logger = logger;
    }

    public Result execute(Teacher teacher, CourseId courseId, Username studentUsername, Grade grade) {
        Course course = courses.findById(courseId).orElse(null);
        if (course == null) return Result.fail("Course not found.");
        if (!course.teachers().contains(teacher.username())) return Result.fail("You are not assigned to this course.");

        User u = users.findByUsername(studentUsername).orElse(null);
        if (!(u instanceof Student student)) return Result.fail("Student not found.");
        if (!course.hasStudent(studentUsername)) return Result.fail("Student is not enrolled in this course.");

        course.recordGrade(studentUsername, grade);
        if (grade.isPassing()) student.recordCompletion(courseId);
        else student.recordFail();

        courses.save(course);
        users.save(student);
        logger.log(teacher.username(), "Recorded marks " + grade.total() + "/" + grade.letter()
                + " for " + studentUsername + " in " + course.name());
        return Result.ok("Marks recorded: total " + grade.total() + " (" + grade.letter() + ")");
    }
}
