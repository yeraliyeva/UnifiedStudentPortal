package domain.user;

import domain.enums.Faculty;
import domain.enums.Gender;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;

public final class EmployeeResearcher extends Employee implements ResearcherCapable {
    private final String defaultField;
    private ResearcherProfile researcherProfile;

    public EmployeeResearcher(Username username, String passwordHash, PersonName name, Gender gender,
                              LocalDate dob, Email email, Faculty faculty,
                              Money salary, LocalDate hireDate, String insuranceNumber,
                              String defaultField) {
        super(username, passwordHash, name, gender, dob, email, faculty, salary, hireDate, insuranceNumber);
        this.defaultField = defaultField;
    }

    public String defaultField() { return defaultField; }

    @Override public boolean isResearcher() { return researcherProfile != null; }
    @Override public void activateResearcher(String field) {
        if (researcherProfile != null) throw new IllegalStateException("already researcher");
        this.researcherProfile = new ResearcherProfile(username(), field);
    }
    @Override public ResearcherProfile researcherProfile() {
        if (researcherProfile == null) throw new IllegalStateException("not a researcher yet");
        return researcherProfile;
    }
}
