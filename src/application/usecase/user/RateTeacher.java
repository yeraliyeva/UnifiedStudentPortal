package application.usecase.user;

import application.Result;
import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import infrastructure.logging.Logger;

public final class RateTeacher {
    private final UserRepository users;
    private final Logger logger;

    public RateTeacher(UserRepository users, Logger logger) {
        this.users = users;
        this.logger = logger;
    }

    public Result execute(Student student, Username teacherUsername, int rating) {
        if (rating < 1 || rating > 10) return Result.fail("Rating must be 1..10.");
        User u = users.findByUsername(teacherUsername).orElse(null);
        if (!(u instanceof Teacher teacher)) return Result.fail("Teacher not found.");
        teacher.addRating(rating);
        users.save(teacher);
        logger.log(student.username(), "Rated teacher " + teacherUsername + ": " + rating);
        return Result.ok(String.format("Rating submitted. Average now %.1f", teacher.averageRating()));
    }
}
