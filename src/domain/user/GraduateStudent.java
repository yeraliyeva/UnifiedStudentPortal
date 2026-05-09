package domain.user;

import domain.enums.DegreeType;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.shared.Email;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;

public final class GraduateStudent extends Student {
    private Username supervisorUsername;

    public GraduateStudent(Username username, String passwordHash, PersonName name, Gender gender,
                           LocalDate dob, Email email, Faculty faculty,
                           DegreeType degreeType, int studyYear) {
        super(username, passwordHash, name, gender, dob, email, faculty, degreeType, studyYear);
        if (degreeType == DegreeType.BACHELOR) {
            throw new IllegalArgumentException("graduate student must be MASTER or DOCTORATE");
        }
    }

    public java.util.Optional<Username> supervisor() { return java.util.Optional.ofNullable(supervisorUsername); }
    public void setSupervisor(Username s) { this.supervisorUsername = s; }
}
