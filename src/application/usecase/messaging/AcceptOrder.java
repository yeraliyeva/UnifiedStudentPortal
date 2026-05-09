package application.usecase.messaging;

import application.Result;
import domain.messaging.Order;
import domain.repository.OrderRepository;
import domain.user.TechSupport;
import infrastructure.logging.Logger;

public final class AcceptOrder {
    private final OrderRepository orders;
    private final Logger logger;

    public AcceptOrder(OrderRepository orders, Logger logger) {
        this.orders = orders;
        this.logger = logger;
    }

    public Result execute(TechSupport tech, int orderId) {
        Order o = orders.findById(orderId).orElse(null);
        if (o == null) return Result.fail("Order not found.");
        o.accept(tech.username());
        orders.save(o);
        logger.log(tech.username(), "Accepted order #" + orderId);
        return Result.ok("Order accepted.");
    }
}
