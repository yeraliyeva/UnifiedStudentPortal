package application.usecase.messaging;

import application.Result;
import domain.messaging.Order;
import domain.repository.OrderRepository;
import domain.shared.IdSequence;
import domain.user.Employee;
import domain.user.User;
import infrastructure.logging.Logger;

public final class CreateITOrder {
    private final OrderRepository orders;
    private final IdSequence ids;
    private final Logger logger;

    public CreateITOrder(OrderRepository orders, IdSequence ids, Logger logger) {
        this.orders = orders;
        this.ids = ids;
        this.logger = logger;
    }

    public Result execute(User user, String description) {
        if (!(user instanceof Employee)) return Result.fail("Only employees can create IT orders.");
        Order o = new Order(ids.next(), user.username(), description);
        orders.save(o);
        logger.log(user.username(), "Created IT order: " + description);
        return Result.ok("Order created: " + o);
    }
}
