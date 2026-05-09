package domain.repository;

import domain.shared.Username;
import domain.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByUsername(Username username);
    boolean exists(Username username);
    void delete(Username username);
    Collection<User> findAll();
}
