package infrastructure.persistence.orm.repository;

import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.User;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.UserMapper;
import infrastructure.persistence.orm.Repository;

import java.util.Collection;
import java.util.Optional;

public final class OrmUserRepository implements UserRepository {
    private final Repository<User, Username> repo;

    public OrmUserRepository(Database db) {
        this.repo = new Repository<>(db, "users", new UserMapper());
    }

    @Override public void save(User user) { repo.save(user); }
    @Override public Optional<User> findByUsername(Username u) { return repo.findById(u); }
    @Override public boolean exists(Username u) { return repo.existsById(u); }
    @Override public void delete(Username u) { repo.deleteById(u); }
    @Override public Collection<User> findAll() { return repo.findAll(); }
}
