package domain.shared;

public record Credits(int value) {
    public static final Credits SEMESTER_LIMIT = new Credits(21);

    public Credits {
        if (value < 0) throw new IllegalArgumentException("credits must be >= 0");
    }

    public boolean covers(Credits required) { return this.value >= required.value; }
    public Credits minus(Credits other) { return new Credits(this.value - other.value); }
    public Credits plus(Credits other) { return new Credits(this.value + other.value); }

    @Override public String toString() { return Integer.toString(value); }
}
