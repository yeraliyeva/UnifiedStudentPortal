package communication;

import enums.OrderStatus;

import java.time.LocalDate;

/**
 * A technical support order (repair / fix request).
 */
public class Order {
    private static int idCounter = 1;

    private final int id;
    private final String requesterUsername;
    private final String description;
    private OrderStatus status;
    private String executorUsername;   // assigned tech support worker
    private final LocalDate createdDate;

    public Order(String requesterUsername, String description) {
        this.id = idCounter++;
        this.requesterUsername = requesterUsername;
        this.description = description;
        this.status = OrderStatus.NEW;
        this.createdDate = LocalDate.now();
    }

    public void accept(String executorUsername) {
        this.executorUsername = executorUsername;
        this.status = OrderStatus.ACCEPTED;
    }

    public void markDone() {
        if (status == OrderStatus.ACCEPTED) {
            status = OrderStatus.DONE;
        } else {
            System.out.println("Order must be accepted before marking as done.");
        }
    }

    public void reject() {
        this.status = OrderStatus.NEW; // stays open for reassignment
        this.executorUsername = null;
    }

    public int getId() { return id; }
    public String getRequesterUsername() { return requesterUsername; }
    public String getDescription() { return description; }
    public OrderStatus getStatus() { return status; }
    public String getExecutorUsername() { return executorUsername; }
    public LocalDate getCreatedDate() { return createdDate; }

    @Override
    public String toString() {
        return String.format("[ORDER-%d] %s | Status: %s | Executor: %s | %s",
                id, description, status,
                executorUsername != null ? executorUsername : "unassigned",
                createdDate);
    }
}
