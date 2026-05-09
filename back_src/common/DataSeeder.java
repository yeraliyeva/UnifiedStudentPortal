package common;

import communication.Organization;
import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import education.Book;
import education.Course;
import education.Lesson;
import education.Specialty;
import enums.*;
import exceptions.LowHIndexException;
import users.*;

import java.time.LocalDate;

/**
 * Seeds the database with demo data so the system is usable out of the box.
 * Also demonstrates usage of ResearcherDecorator.
 */
public class DataSeeder {

    public static void seed() {
        Database db = Database.getInstance();

        // ── Admin ─────────────────────────────────────────────────
        Admin admin = new Admin("System", "Admin", "admin", "admin123",
                Gender.UNDEFINED, LocalDate.of(1980, 1, 1),
                "admin@uni.edu", Faculty.SITE);
        db.addUser(admin);

        // ── Teachers ──────────────────────────────────────────────
        Teacher teacher1 = admin.createTeacher("Alice", "Smith", "alice",
                "pass123", Gender.FEMALE, LocalDate.of(1985, 3, 15),
                "alice@uni.edu", Faculty.SITE, 120000,
                LocalDate.of(2015, 9, 1), "INS-T001", "PhD", true);

        Teacher teacher2 = admin.createTeacher("Bob", "Jones", "bob",
                "pass123", Gender.MALE, LocalDate.of(1978, 7, 22),
                "bob@uni.edu", Faculty.SITE, 110000,
                LocalDate.of(2010, 9, 1), "INS-T002", "MSc", false);

        // ── Dean ──────────────────────────────────────────────────
        Dean dean = admin.createDean("Carol", "Dean", "carol",
                "pass123", Gender.FEMALE, LocalDate.of(1975, 5, 10),
                "carol@uni.edu", Faculty.SITE, 160000,
                LocalDate.of(2005, 9, 1), "INS-D001", "PhD");

        // ── Manager ───────────────────────────────────────────────
        Manager manager = admin.createManager("David", "Mgr", "david",
                "pass123", Gender.MALE, LocalDate.of(1982, 11, 3),
                "david@uni.edu", Faculty.SITE, 95000,
                LocalDate.of(2018, 1, 15), "INS-M001", ManagerPosition.OR);

        // ── Students ──────────────────────────────────────────────
        Student student1 = admin.createStudent("Eve", "Johnson", "eve",
                "pass123", Gender.FEMALE, LocalDate.of(2002, 4, 20),
                "eve@uni.edu", Faculty.SITE, DegreeType.BACHELOR, 2);

        Student student2 = admin.createStudent("Frank", "Brown", "frank",
                "pass123", Gender.MALE, LocalDate.of(2001, 8, 14),
                "frank@uni.edu", Faculty.SITE, DegreeType.BACHELOR, 3);

        GraduateStudent gradStudent = admin.createGraduateStudent("Grace", "Lee", "grace",
                "pass123", Gender.FEMALE, LocalDate.of(1999, 6, 5),
                "grace@uni.edu", Faculty.SITE, DegreeType.MASTER, 1);

        // ── Librarian ─────────────────────────────────────────────
        Librarian librarian = admin.createLibrarian("Henry", "Lib", "henry",
                "pass123", Gender.MALE, LocalDate.of(1990, 2, 28),
                "henry@uni.edu", Faculty.SITE, 70000,
                LocalDate.of(2019, 3, 1), "INS-L001");

        // ── Tech Support ──────────────────────────────────────────
        TechSupport tech = admin.createTechSupport("Iris", "Tech", "iris",
                "pass123", Gender.FEMALE, LocalDate.of(1995, 9, 17),
                "iris@uni.edu", Faculty.SITE, 72000,
                LocalDate.of(2021, 6, 1), "INS-TS001");

        // ── Employee Researcher ───────────────────────────────────
        EmployeeResearcher empRes = admin.createEmployeeResearcher(
                "Jack", "Research", "jack", "pass123", Gender.MALE,
                LocalDate.of(1988, 12, 12), "jack@uni.edu", Faculty.SITE,
                98000, LocalDate.of(2017, 4, 1), "INS-ER001", "Machine Learning");

        // ── Courses ───────────────────────────────────────────────
        Course oop = new Course("Object-Oriented Programming", 5, DisciplineType.MAJOR);
        Course math = new Course("Discrete Mathematics", 4, DisciplineType.MAJOR);
        Course elec = new Course("Introduction to AI", 3, DisciplineType.FREE);
        db.addCourse(oop);
        db.addCourse(math);
        db.addCourse(elec);

        // assign teachers
        teacher1.assignToCourse(oop);
        teacher2.assignToCourse(math);
        teacher1.assignToCourse(elec);

        // add lessons
        oop.addLesson(new Lesson(LessonType.LECTURE,   WeekDay.MONDAY,    "09:00", "Room 101"));
        oop.addLesson(new Lesson(LessonType.PRACTICE,  WeekDay.WEDNESDAY,  "11:00", "Lab 3"));
        oop.addLesson(new Lesson(LessonType.OFFICE_HOURS, WeekDay.FRIDAY, "14:00", "Office 205"));
        math.addLesson(new Lesson(LessonType.LECTURE,  WeekDay.TUESDAY,   "10:00", "Room 202"));
        math.addLesson(new Lesson(LessonType.PRACTICE, WeekDay.THURSDAY,   "12:00", "Room 202"));

        // enroll students
        student1.enrollCourse(oop);
        student1.enrollCourse(math);
        student2.enrollCourse(oop);
        gradStudent.enrollCourse(elec);

        // set some grades
        oop.setGrade("eve", 25, 24, 35);
        oop.setGrade("frank", 20, 22, 30);
        math.setGrade("eve", 28, 27, 38);

        // ── Specialty ─────────────────────────────────────────────
        Specialty cs = new Specialty("CS-001", "Computer Science", Faculty.SITE);
        cs.addCourse(oop);
        cs.addCourse(math);
        db.addSpecialty(cs);

        // ── Books ─────────────────────────────────────────────────
        db.addBook(new Book("Clean Code", "Robert C. Martin"));
        db.addBook(new Book("Design Patterns", "Gang of Four"));
        db.addBook(new Book("Introduction to Algorithms", "Cormen et al."));
        db.addBook(new Book("Head First Java", "Kathy Sierra"));

        // ── Research ──────────────────────────────────────────────
        ResearchProject project = new ResearchProject("SITE Journal", "AI in Education", "alice");
        db.addResearchProject(project);

        // Teacher alice has papers — citations: 5,4,3,2 → h-index = 3
        int[] citationsPerPaper = {5, 4, 3, 2};
        for (int i = 0; i < citationsPerPaper.length; i++) {
            ResearchPaper paper = new ResearchPaper(
                    "AI Paper #" + (i + 1), "alice", "SITE Journal",
                    "This paper discusses topic " + (i + 1) + " in detail with extensive analysis.",
                    10 + i, "10.1234/ai." + (i + 1));
            for (int c = 0; c < citationsPerPaper[i]; c++) paper.incrementCitations();
            teacher1.addResearchPaper(paper);
            project.publishPaper(paper);
        }

        // Set grace's supervisor (alice has h-index = 3)
        try {
            gradStudent.setSupervisor(teacher1);
        } catch (LowHIndexException e) {
            System.out.println("Warning: " + e.getMessage());
        }

        // Subscribe grace to the journal
        project.subscribe(gradStudent);

        // ── Decorator Demo: give Bachelor student "eve" research abilities ──
        ResearcherDecorator eveResearcher = new ResearcherDecorator(student1);
        ResearchPaper evePaper = new ResearchPaper("Student Research on OOP", "eve",
                "Student Journal", "An exploration of object-oriented design principles.");
        eveResearcher.addResearchPaper(evePaper);
        // store decorator reference in db for lookup if needed
        System.out.println("Decorator demo: " + eveResearcher);

        // ── Student Organizations ─────────────────────────────────
        Organization acm = new Organization("ACM Student Chapter", "eve");
        acm.addMember("frank");
        acm.addMember("grace");
        db.addOrganization(acm);

        Organization ai_club = new Organization("AI Research Club", "grace");
        ai_club.addMember("eve");
        db.addOrganization(ai_club);

        // ── Teacher ratings demo ──────────────────────────────────
        teacher1.addRating(9);
        teacher1.addRating(8);
        teacher1.addRating(10);
        teacher2.addRating(7);
        teacher2.addRating(6);

        // ── Auto-generate top researcher news ─────────────────────
        db.generateTopResearcherNews();

        System.out.println("\n--- Demo data loaded. ---");
        System.out.println("Accounts: admin/admin123, alice/pass123, bob/pass123,");
        System.out.println("          carol/pass123, david/pass123, eve/pass123,");
        System.out.println("          frank/pass123, grace/pass123, henry/pass123,");
        System.out.println("          iris/pass123, jack/pass123");
        System.out.println("---\n");
    }
}
