package domain.shared;

public record PersonName(String first, String last) {
    public PersonName {
        if (first == null || first.isBlank()) throw new IllegalArgumentException("first name");
        if (last == null || last.isBlank()) throw new IllegalArgumentException("last name");
    }
    public String full() { return first + " " + last; }
}
