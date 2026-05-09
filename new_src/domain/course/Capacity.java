package domain.course;

public record Capacity(int max) {
    public static final Capacity UNLIMITED = new Capacity(1000);

    public Capacity {
        if (max < 1) throw new IllegalArgumentException("capacity must be >= 1");
    }

    public boolean canFit(int currentEnrolled) { return currentEnrolled < max; }
    public int remaining(int currentEnrolled) { return Math.max(0, max - currentEnrolled); }
}
