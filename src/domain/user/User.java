package domain.user;

import domain.enums.Faculty;
import domain.enums.Gender;
import domain.enums.Language;
import domain.shared.Email;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;
import java.util.Objects;

public abstract class User {
    private final Username username;
    private String passwordHash;
    private final PersonName name;
    private final Gender gender;
    private final LocalDate dateOfBirth;
    private Email email;
    private final Faculty faculty;
    private Language language;

    protected User(Username username, String passwordHash, PersonName name, Gender gender,
                   LocalDate dateOfBirth, Email email, Faculty faculty) {
        this.username = Objects.requireNonNull(username);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.name = Objects.requireNonNull(name);
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.faculty = faculty;
        this.language = Language.ENGLISH;
    }

    public Username username() { return username; }
    public String passwordHash() { return passwordHash; }
    public PersonName name() { return name; }
    public Gender gender() { return gender; }
    public LocalDate dateOfBirth() { return dateOfBirth; }
    public Email email() { return email; }
    public Faculty faculty() { return faculty; }
    public Language language() { return language; }

    public void changePassword(String newHash) { this.passwordHash = newHash; }
    public void changeEmail(Email newEmail) { this.email = newEmail; }
    public void changeLanguage(Language lang) { this.language = lang; }

    public boolean matchesPassword(String hash) { return passwordHash.equals(hash); }

    @Override public boolean equals(Object o) {
        return o instanceof User u && username.equals(u.username);
    }
    @Override public int hashCode() { return username.hashCode(); }
    @Override public String toString() { return username + " (" + name.full() + ") [" + getClass().getSimpleName() + "]"; }
}
