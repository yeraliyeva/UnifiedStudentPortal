import education.Course;
import enums.DisciplineType;
import enums.Faculty;
import enums.Gender;
import enums.TeacherPosition;
import users.Teacher;
import java.time.LocalDate;

public class test_repro {
    public static void main(String[] args) {
        Course course = new Course("Software Engineering", 5, DisciplineType.MAJOR);
        Teacher teacher = new Teacher("T", "T", "tch1", "p", Gender.FEMALE, LocalDate.now(), "e", Faculty.SITE, 2000, LocalDate.now(), "INS", "PhD", TeacherPosition.PROFESSOR);
        
        System.out.println("Before assign: teacher courses size=" + teacher.getTaughtCourses().size() + ", course teachers size=" + course.getTeachers().size());
        System.out.println("Course teachers contains teacher? " + course.getTeachers().contains(teacher));
        teacher.assignToCourse(course);
        System.out.println("After assign: teacher courses size=" + teacher.getTaughtCourses().size() + ", course teachers size=" + course.getTeachers().size());
    }
}
