package domain.research;

public record JournalName(String value) {
    public JournalName {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("journal name");
    }
    public boolean matches(String other) { return value.equalsIgnoreCase(other); }
    @Override public String toString() { return value; }
}
