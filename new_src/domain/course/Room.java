package domain.course;

public record Room(String name) {
    public Room {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("room");
    }
    public boolean sameAs(Room other) { return name.equalsIgnoreCase(other.name); }
    @Override public String toString() { return name; }
}
