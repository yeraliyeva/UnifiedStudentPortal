package application.usecase.user;

import application.Result;
import application.usecase.messaging.SendMessage;
import domain.enums.UrgencyLevel;
import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.Dean;
import domain.user.Teacher;
import domain.user.User;
import infrastructure.logging.Logger;

public final class ComplainAboutStudent {
    private final SendMessage sendMessage;
    private final UserRepository users;
    private final Logger logger;

    public ComplainAboutStudent(SendMessage sendMessage, UserRepository users, Logger logger) {
        this.sendMessage = sendMessage;
        this.users = users;
        this.logger = logger;
    }

    public Result execute(Teacher teacher, Username studentUsername, String reason, UrgencyLevel urgency) {
        if (!users.exists(studentUsername)) return Result.fail("Student not found.");
        Dean dean = users.findAll().stream()
                .filter(u -> u instanceof Dean && u.faculty() == teacher.faculty())
                .map(u -> (Dean) u)
                .findFirst().orElse(null);
        if (dean == null) return Result.fail("No dean found for faculty " + teacher.faculty());
        Result r = sendMessage.execute(teacher.username(), dean.username(),
                "Complaint about student: " + studentUsername, reason, urgency);
        if (r.success()) logger.log(teacher.username(), "Complained about " + studentUsername + " to dean " + dean.username());
        return r;
    }
}
