package domain.user;

import domain.enums.Faculty;
import domain.enums.Gender;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;

public abstract class Employee extends User {
    private final Money salary;
    private final LocalDate hireDate;
    private final String insuranceNumber;

    protected Employee(Username username, String passwordHash, PersonName name, Gender gender,
                       LocalDate dob, Email email, Faculty faculty,
                       Money salary, LocalDate hireDate, String insuranceNumber) {
        super(username, passwordHash, name, gender, dob, email, faculty);
        this.salary = salary;
        this.hireDate = hireDate;
        this.insuranceNumber = insuranceNumber;
    }

    public Money salary() { return salary; }
    public LocalDate hireDate() { return hireDate; }
    public String insuranceNumber() { return insuranceNumber; }
}
