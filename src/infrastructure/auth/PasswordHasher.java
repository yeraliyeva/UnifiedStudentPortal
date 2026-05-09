package infrastructure.auth;

public interface PasswordHasher {
    String hash(String plain);
    boolean matches(String plain, String hash);
}
