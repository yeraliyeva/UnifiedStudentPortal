package infrastructure.persistence.inmemory;

import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryCourseRepository implements CourseRepository {
    private final Map<CourseId, Course> store = new LinkedHashMap<>();

    @Override public void save(Course course) { store.put(course.id(), course); }
    @Override public Optional<Course> findById(CourseId id) { return Optional.ofNullable(store.get(id)); }
    @Override public Collection<Course> findAll() { return Collections.unmodifiableCollection(store.values()); }
}
