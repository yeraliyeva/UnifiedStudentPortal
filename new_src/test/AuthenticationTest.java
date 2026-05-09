package test;

import domain.repository.UserRepository;
import domain.user.User;
import infrastructure.auth.AuthenticationService;
import infrastructure.auth.PlainPasswordHasher;
import infrastructure.persistence.inmemory.InMemoryUserRepository;

import java.util.Optional;

public final class AuthenticationTest {

    public static void runAll() {
        TestRunner.run("Auth: correct credentials -> user", AuthenticationTest::testGood);
        TestRunner.run("Auth: wrong password -> empty", AuthenticationTest::testBadPassword);
        TestRunner.run("Auth: unknown username -> empty", AuthenticationTest::testUnknown);
    }

    private static AuthenticationService service(UserRepository repo) {
        return new AuthenticationService(repo, new PlainPasswordHasher());
    }

    private static void testGood() {
        UserRepository users = new InMemoryUserRepository();
        users.save(Fixtures.student("eve"));
        Optional<User> u = service(users).authenticate("eve", "p");
        Assert.isTrue(u.isPresent(), "Should authenticate with correct password");
    }

    private static void testBadPassword() {
        UserRepository users = new InMemoryUserRepository();
        users.save(Fixtures.student("eve"));
        Optional<User> u = service(users).authenticate("eve", "wrong");
        Assert.isFalse(u.isPresent(), "Wrong password should reject");
    }

    private static void testUnknown() {
        UserRepository users = new InMemoryUserRepository();
        Optional<User> u = service(users).authenticate("ghost", "p");
        Assert.isFalse(u.isPresent(), "Unknown user should reject");
    }
}
