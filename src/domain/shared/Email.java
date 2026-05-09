package domain.shared;

public record Email(String address) {
    public Email {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("email");
    }
    @Override public String toString() { return address; }
}
