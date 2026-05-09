package domain.user;

import domain.course.CourseId;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.enums.TeacherPosition;
import domain.shared.Email;
import domain.shared.Money;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Teacher extends Employee implements BookBorrowerCapable, ResearcherCapable {
    private final String degree;
    private TeacherPosition position;
    private final Set<CourseId> taughtCourses = new LinkedHashSet<>();
    private final List<Integer> ratings = new ArrayList<>();
    private ResearcherProfile researcherProfile;

    public Teacher(Username username, String passwordHash, PersonName name, Gender gender,
                   LocalDate dob, Email email, Faculty faculty,
                   Money salary, LocalDate hireDate, String insuranceNumber,
                   String degree, TeacherPosition position) {
        super(username, passwordHash, name, gender, dob, email, faculty, salary, hireDate, insuranceNumber);
        this.degree = degree;
        this.position = position;
    }

    public String degree() { return degree; }
    public TeacherPosition position() { return position; }
    public void setPosition(TeacherPosition p) { this.position = p; }

    public Set<CourseId> taughtCourses() { return Collections.unmodifiableSet(taughtCourses); }
    public void recordCourseAssignment(CourseId courseId) { taughtCourses.add(courseId); }

    public void rehydrate(Iterable<CourseId> taughtIds, Iterable<Integer> ratingValues) {
        this.taughtCourses.clear();
        taughtIds.forEach(this.taughtCourses::add);
        this.ratings.clear();
        ratingValues.forEach(this.ratings::add);
    }

    public void addRating(int r) { ratings.add(Math.max(1, Math.min(10, r))); }
    public List<Integer> ratings() { return Collections.unmodifiableList(ratings); }
    public double averageRating() {
        return ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

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
