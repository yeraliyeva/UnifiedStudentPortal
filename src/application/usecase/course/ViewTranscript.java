package application.usecase.course;

import domain.course.Course;
import domain.course.CourseId;
import domain.course.Grade;
import domain.repository.CourseRepository;
import domain.service.GpaCalculator;
import domain.user.Student;

import java.util.ArrayList;
import java.util.List;

public final class ViewTranscript {
    public record TranscriptLine(String courseName, String letter, int total) {}
    public record Transcript(String fullName, String degreeType, int year,
                             int failCount, double gpa, List<TranscriptLine> lines) {}

    private final CourseRepository courses;
    private final GpaCalculator gpa;

    public ViewTranscript(CourseRepository courses, GpaCalculator gpa) {
        this.courses = courses;
        this.gpa = gpa;
    }

    public Transcript execute(Student student) {
        List<TranscriptLine> lines = new ArrayList<>();
        for (CourseId id : student.enrolledCourses()) {
            Course c = courses.findById(id).orElse(null);
            if (c == null) continue;
            Grade g = c.gradeOf(student.username()).orElse(null);
            if (g == null) lines.add(new TranscriptLine(c.name(), "—", 0));
            else lines.add(new TranscriptLine(c.name(), g.letter(), g.total()));
        }
        return new Transcript(student.name().full(), student.degreeType().name(),
                student.studyYear(), student.failCount(), gpa.of(student), lines);
    }
}
