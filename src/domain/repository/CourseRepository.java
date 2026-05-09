package domain.repository;

import domain.course.Course;
import domain.course.CourseId;

import java.util.Collection;
import java.util.Optional;

public interface CourseRepository {
    void save(Course course);
    Optional<Course> findById(CourseId id);
    Collection<Course> findAll();
}
