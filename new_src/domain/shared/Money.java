package domain.shared;

public record Money(double amount) {
    public Money {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
    }
    @Override public String toString() { return String.format("%.2f", amount); }
}
