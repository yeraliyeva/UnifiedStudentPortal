package infrastructure.persistence.orm.repository;

import domain.course.Course;
import domain.course.CourseId;
import domain.repository.CourseRepository;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.CourseMapper;
import infrastructure.persistence.orm.Repository;

import java.util.Collection;
import java.util.Optional;

public final class OrmCourseRepository implements CourseRepository {
    private final Repository<Course, CourseId> repo;

    public OrmCourseRepository(Database db) {
        this.repo = new Repository<>(db, "courses", new CourseMapper());
    }

    @Override public void save(Course course) { repo.save(course); }
    @Override public Optional<Course> findById(CourseId id) { return repo.findById(id); }
    @Override public Collection<Course> findAll() { return repo.findAll(); }
}
