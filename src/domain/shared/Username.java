package domain.shared;

import java.util.Objects;

public final class Username {
    private final String value;

    public Username(String value) {
        Objects.requireNonNull(value, "username");
        if (value.isBlank()) throw new IllegalArgumentException("username must not be blank");
        this.value = value.trim();
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Username u)) return false;
        return value.equals(u.value);
    }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
