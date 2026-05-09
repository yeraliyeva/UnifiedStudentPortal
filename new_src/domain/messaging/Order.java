package domain.messaging;

import domain.enums.OrderStatus;
import domain.shared.Username;

import java.time.LocalDate;

public final class Order {
    private final int id;
    private final Username requester;
    private final String description;
    private final LocalDate createdAt;
    private OrderStatus status = OrderStatus.NEW;
    private Username executor;

    public Order(int id, Username requester, String description) {
        this.id = id;
        this.requester = requester;
        this.description = description;
        this.createdAt = LocalDate.now();
    }

    public int id() { return id; }
    public Username requester() { return requester; }
    public String description() { return description; }
    public LocalDate createdAt() { return createdAt; }
    public OrderStatus status() { return status; }
    public java.util.Optional<Username> executor() { return java.util.Optional.ofNullable(executor); }

    public void accept(Username executor) { this.executor = executor; this.status = OrderStatus.ACCEPTED; }
    public void reject() { this.status = OrderStatus.REJECTED; }
    public void complete() { if (status != OrderStatus.ACCEPTED) throw new IllegalStateException("must accept first"); this.status = OrderStatus.DONE; }

    @Override public String toString() {
        return "[ORDER-" + id + "/" + status + "] " + description + " | requester: " + requester
                + " | executor: " + (executor == null ? "unassigned" : executor) + " | " + createdAt;
    }
}
