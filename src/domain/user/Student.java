package domain.user;

import domain.course.CourseId;
import domain.enums.DegreeType;
import domain.enums.Faculty;
import domain.enums.Gender;
import domain.shared.Credits;
import domain.shared.Email;
import domain.shared.PersonName;
import domain.shared.Username;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Student extends User implements BookBorrowerCapable, ResearcherCapable {
    public static final int MAX_FAILS = 3;

    private final DegreeType degreeType;
    private final int studyYear;
    private Credits availableCredits = Credits.SEMESTER_LIMIT;
    private int failCount;

    private final Set<CourseId> enrolled = new LinkedHashSet<>();
    private final Set<CourseId> completed = new LinkedHashSet<>();

    private ResearcherProfile researcherProfile;

    public Student(Username username, String passwordHash, PersonName name, Gender gender,
                   LocalDate dob, Email email, Faculty faculty,
                   DegreeType degreeType, int studyYear) {
        super(username, passwordHash, name, gender, dob, email, faculty);
        this.degreeType = degreeType;
        this.studyYear = studyYear;
    }

    public DegreeType degreeType() { return degreeType; }
    public int studyYear() { return studyYear; }
    public Credits availableCredits() { return availableCredits; }
    public int failCount() { return failCount; }
    public boolean hasReachedFailLimit() { return failCount >= MAX_FAILS; }

    public Set<CourseId> enrolledCourses() { return Collections.unmodifiableSet(enrolled); }
    public Set<CourseId> completedCourses() { return Collections.unmodifiableSet(completed); }

    public void recordEnrollment(CourseId courseId, Credits cost) {
        enrolled.add(courseId);
        availableCredits = availableCredits.minus(cost);
    }
    public void recordDrop(CourseId courseId, Credits refund) {
        enrolled.remove(courseId);
        availableCredits = availableCredits.plus(refund);
    }
    public void recordCompletion(CourseId courseId) { completed.add(courseId); }
    public void recordFail() { failCount++; }

    public void rehydrate(int availableCreditsValue, int failCountValue,
                          Iterable<CourseId> enrolledIds, Iterable<CourseId> completedIds) {
        this.availableCredits = new Credits(availableCreditsValue);
        this.failCount = failCountValue;
        this.enrolled.clear();
        enrolledIds.forEach(this.enrolled::add);
        this.completed.clear();
        completedIds.forEach(this.completed::add);
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
