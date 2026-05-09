package bootstrap;

import domain.course.Capacity;
import domain.course.Course;
import domain.course.CourseId;
import domain.enums.DegreeType;
import domain.enums.DisciplineType;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.enums.ManagerPosition;
import domain.enums.TeacherPosition;
import domain.library.Book;
import domain.library.BookId;
import domain.shared.Credits;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;
import domain.user.Admin;
import domain.user.Librarian;
import domain.user.Manager;
import domain.user.Student;
import domain.user.Teacher;

import java.time.LocalDate;

public final class DataSeeder {
    private final AppContext ctx;

    public DataSeeder(AppContext ctx) { this.ctx = ctx; }

    public void seedIfEmpty() {
        if (!ctx.userRepository.findAll().isEmpty()) return;
        seedAdmin();
        seedManager();
        seedTeacher();
        seedStudent();
        seedLibrarian();
        seedCourses();
        seedBooks();
    }

    private void seedAdmin() {
        Admin admin = new Admin(new Username("admin"), ctx.passwordHasher.hash("admin"),
                new PersonName("System", "Admin"), Gender.MALE, LocalDate.of(1980, 1, 1),
                new Email("admin@uni.edu"), Faculty.SITE);
        ctx.userRepository.save(admin);
    }

    private void seedManager() {
        Manager mgr = new Manager(new Username("dave"), ctx.passwordHasher.hash("dave"),
                new PersonName("Dave", "Johnson"), Gender.MALE, LocalDate.of(1985, 5, 12),
                new Email("dave@uni.edu"), Faculty.SITE,
                new Money(2000), LocalDate.of(2020, 3, 1), "INS-001", ManagerPosition.OR);
        ctx.userRepository.save(mgr);
    }

    private void seedTeacher() {
        Teacher teacher = new Teacher(new Username("bob"), ctx.passwordHasher.hash("bob"),
                new PersonName("Bob", "Ross"), Gender.MALE, LocalDate.of(1975, 11, 4),
                new Email("bob@uni.edu"), Faculty.SITE,
                new Money(2500), LocalDate.of(2018, 9, 1), "INS-002", "PhD", TeacherPosition.PROFESSOR);
        ctx.userRepository.save(teacher);
    }

    private void seedStudent() {
        Student stu = new Student(new Username("eve"), ctx.passwordHasher.hash("eve"),
                new PersonName("Eve", "Smith"), Gender.FEMALE, LocalDate.of(2003, 6, 15),
                new Email("eve@uni.edu"), Faculty.SITE, DegreeType.BACHELOR, 2);
        ctx.userRepository.save(stu);
    }

    private void seedLibrarian() {
        Librarian lib = new Librarian(new Username("lib"), ctx.passwordHasher.hash("lib"),
                new PersonName("Library", "Worker"), Gender.FEMALE, LocalDate.of(1990, 2, 20),
                new Email("lib@uni.edu"), Faculty.SITE,
                new Money(1500), LocalDate.of(2019, 4, 1), "INS-003");
        ctx.userRepository.save(lib);
    }

    private void seedCourses() {
        Course math = new Course(CourseId.of(ctx.courseIds.next()), "Math", new Credits(5), DisciplineType.MAJOR, new Capacity(50));
        Course oop  = new Course(CourseId.of(ctx.courseIds.next()), "OOP",  new Credits(5), DisciplineType.MAJOR, new Capacity(30));
        oop.addPrerequisite(math.id());
        ctx.courseRepository.save(math);
        ctx.courseRepository.save(oop);
    }

    private void seedBooks() {
        ctx.bookRepository.save(new Book(new BookId(ctx.bookIds.next()), "Clean Code", "Robert Martin"));
        ctx.bookRepository.save(new Book(new BookId(ctx.bookIds.next()), "Effective Java", "Joshua Bloch"));
    }
}
