package domain.research;

public record HIndex(int value) {
    public static final HIndex MIN_FOR_SUPERVISION = new HIndex(3);

    public HIndex {
        if (value < 0) throw new IllegalArgumentException("hIndex must be >= 0");
    }
    public boolean atLeast(HIndex other) { return this.value >= other.value; }
}
