package domain.research;

public record PaperId(int value) {
    public PaperId {
        if (value < 1) throw new IllegalArgumentException("paperId");
    }
    @Override public String toString() { return "PAPER-" + value; }
}
