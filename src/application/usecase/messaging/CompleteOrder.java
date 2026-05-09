package application.usecase.messaging;

import application.Result;
import domain.enums.UrgencyLevel;
import domain.messaging.Message;
import domain.messaging.Order;
import domain.repository.MessageRepository;
import domain.repository.OrderRepository;
import domain.shared.IdSequence;
import domain.user.TechSupport;
import infrastructure.logging.Logger;

public final class CompleteOrder {
    private final OrderRepository orders;
    private final MessageRepository messages;
    private final IdSequence msgIds;
    private final Logger logger;

    public CompleteOrder(OrderRepository orders, MessageRepository messages, IdSequence msgIds, Logger logger) {
        this.orders = orders;
        this.messages = messages;
        this.msgIds = msgIds;
        this.logger = logger;
    }

    public Result execute(TechSupport tech, int orderId) {
        Order o = orders.findById(orderId).orElse(null);
        if (o == null) return Result.fail("Order not found.");
        o.complete();
        orders.save(o);
        messages.save(new Message(msgIds.next(), tech.username(), o.requester(),
                "Order #" + o.id() + " completed",
                "Your support order has been completed: " + o.description(),
                UrgencyLevel.LOW));
        logger.log(tech.username(), "Completed order #" + orderId);
        return Result.ok("Order completed.");
    }
}
