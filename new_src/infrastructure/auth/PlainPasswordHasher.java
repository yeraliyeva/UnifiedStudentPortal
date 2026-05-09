package infrastructure.auth;

public final class PlainPasswordHasher implements PasswordHasher {
    @Override public String hash(String plain) { return plain; }
    @Override public boolean matches(String plain, String hash) { return plain.equals(hash); }
}
