package domain.user;

import domain.enums.Faculty;
import domain.enums.Gender;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;

public final class TechSupport extends Employee {
    public TechSupport(Username username, String passwordHash, PersonName name, Gender gender,
                       LocalDate dob, Email email, Faculty faculty,
                       Money salary, LocalDate hireDate, String insuranceNumber) {
        super(username, passwordHash, name, gender, dob, email, faculty, salary, hireDate, insuranceNumber);
    }
}
