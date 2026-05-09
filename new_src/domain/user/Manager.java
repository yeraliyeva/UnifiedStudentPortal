package domain.user;

import domain.enums.Faculty;
import domain.enums.Gender;
import domain.enums.ManagerPosition;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;

public final class Manager extends Employee {
    private final ManagerPosition position;

    public Manager(Username username, String passwordHash, PersonName name, Gender gender,
                   LocalDate dob, Email email, Faculty faculty,
                   Money salary, LocalDate hireDate, String insuranceNumber,
                   ManagerPosition position) {
        super(username, passwordHash, name, gender, dob, email, faculty, salary, hireDate, insuranceNumber);
        this.position = position;
    }

    public ManagerPosition position() { return position; }
}
