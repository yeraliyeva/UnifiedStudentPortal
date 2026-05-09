package application.usecase.admin;

import domain.enums.DegreeType;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.repository.UserRepository;
import domain.shared.Email;
import domain.shared.PersonName;
import domain.shared.Username;
import domain.user.Student;
import infrastructure.auth.PasswordHasher;
import infrastructure.logging.Logger;

import java.time.LocalDate;

public final class CreateStudent {
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Logger logger;

    public CreateStudent(UserRepository users, PasswordHasher hasher, Logger logger) {
        this.users = users;
        this.hasher = hasher;
        this.logger = logger;
    }

    public Student execute(Username actor, String first, String last, String username, String password,
                           Gender gender, LocalDate dob, String email, Faculty faculty,
                           DegreeType degreeType, int studyYear) {
        Student s = new Student(new Username(username), hasher.hash(password),
                new PersonName(first, last), gender, dob, new Email(email), faculty, degreeType, studyYear);
        users.save(s);
        logger.log(actor, "Created student: " + username);
        return s;
    }
}
