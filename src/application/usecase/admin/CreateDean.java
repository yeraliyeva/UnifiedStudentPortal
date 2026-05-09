package application.usecase.admin;

import domain.enums.Faculty;
import domain.enums.Gender;
import domain.repository.UserRepository;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;
import domain.user.Dean;
import infrastructure.auth.PasswordHasher;
import infrastructure.logging.Logger;

import java.time.LocalDate;

public final class CreateDean {
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Logger logger;

    public CreateDean(UserRepository users, PasswordHasher hasher, Logger logger) {
        this.users = users;
        this.hasher = hasher;
        this.logger = logger;
    }

    public Dean execute(Username actor, String first, String last, String username, String password,
                        Gender gender, LocalDate dob, String email, Faculty faculty,
                        double salary, LocalDate hireDate, String insurance, String degree) {
        Dean d = new Dean(new Username(username), hasher.hash(password),
                new PersonName(first, last), gender, dob, new Email(email), faculty,
                new Money(salary), hireDate, insurance, degree);
        users.save(d);
        logger.log(actor, "Created dean: " + username);
        return d;
    }
}
