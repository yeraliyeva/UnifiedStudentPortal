package domain.user;

import domain.enums.Faculty;
import domain.enums.Gender;
import domain.shared.Email;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;

public final class Admin extends User {
    public Admin(Username username, String passwordHash, PersonName name, Gender gender,
                 LocalDate dob, Email email, Faculty faculty) {
        super(username, passwordHash, name, gender, dob, email, faculty);
    }
}
