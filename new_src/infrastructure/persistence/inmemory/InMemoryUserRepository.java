package infrastructure.persistence.inmemory;

import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryUserRepository implements UserRepository {
    private final Map<Username, User> store = new LinkedHashMap<>();

    @Override public void save(User user) { store.put(user.username(), user); }
    @Override public Optional<User> findByUsername(Username u) { return Optional.ofNullable(store.get(u)); }
    @Override public boolean exists(Username u) { return store.containsKey(u); }
    @Override public void delete(Username u) { store.remove(u); }
    @Override public Collection<User> findAll() { return Collections.unmodifiableCollection(store.values()); }
}
