package test;

import domain.course.Capacity;
import domain.course.Course;
import domain.course.CourseId;
import domain.enums.DegreeType;
import domain.enums.DisciplineType;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.enums.TeacherPosition;
import domain.shared.Credits;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;
import domain.user.Student;
import domain.user.Teacher;

import java.time.LocalDate;

public final class Fixtures {
    private Fixtures() {}

    public static Student student(String username) {
        return new Student(new Username(username), "p", new PersonName("F", "L"),
                Gender.MALE, LocalDate.of(2003, 1, 1), new Email("e@e.com"),
                Faculty.SITE, DegreeType.BACHELOR, 1);
    }

    public static Teacher teacher(String username) {
        return new Teacher(new Username(username), "p", new PersonName("T", "T"),
                Gender.MALE, LocalDate.of(1970, 1, 1), new Email("t@e.com"),
                Faculty.SITE, new Money(2000), LocalDate.now(), "INS", "PhD", TeacherPosition.PROFESSOR);
    }

    public static Course course(String name, int credits, int capacity, int idSeq) {
        return new Course(CourseId.of(idSeq), name, new Credits(credits), DisciplineType.MAJOR, new Capacity(capacity));
    }
}
