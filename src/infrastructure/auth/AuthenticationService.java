package infrastructure.auth;

import domain.repository.UserRepository;
import domain.shared.Username;
import domain.user.User;

import java.util.Optional;

public final class AuthenticationService {
    private final UserRepository users;
    private final PasswordHasher hasher;

    public AuthenticationService(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public Optional<User> authenticate(String username, String password) {
        try {
            return users.findByUsername(new Username(username))
                    .filter(u -> hasher.matches(password, u.passwordHash()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
