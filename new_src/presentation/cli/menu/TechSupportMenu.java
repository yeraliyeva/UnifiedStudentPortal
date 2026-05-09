package presentation.cli.menu;

import application.Result;
import application.usecase.messaging.AcceptOrder;
import application.usecase.messaging.CompleteOrder;
import domain.enums.OrderStatus;
import domain.repository.OrderRepository;
import domain.user.TechSupport;
import presentation.cli.Console;

import java.util.List;

public final class TechSupportMenu extends Menu {
    private final TechSupport tech;
    private final OrderRepository orders;
    private final AcceptOrder acceptOrder;
    private final CompleteOrder completeOrder;

    public TechSupportMenu(Console console, TechSupport tech, OrderRepository orders,
                           AcceptOrder acceptOrder, CompleteOrder completeOrder) {
        super(console);
        this.tech = tech;
        this.orders = orders;
        this.acceptOrder = acceptOrder;
        this.completeOrder = completeOrder;
    }

    @Override protected String title() { return "=== TECH SUPPORT MENU (" + tech.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        return List.of(
                new MenuItem("View new orders", this::viewNewOrders),
                new MenuItem("Accept an order", this::acceptInteractive),
                new MenuItem("Complete an order", this::completeInteractive)
        );
    }

    private void viewNewOrders() {
        var list = orders.findAll().stream().filter(o -> o.status() == OrderStatus.NEW).toList();
        if (list.isEmpty()) { console.println("No new orders."); return; }
        list.forEach(o -> console.println(o.toString()));
    }

    private void acceptInteractive() {
        int id = console.readInt("Order ID:");
        Result r = acceptOrder.execute(tech, id);
        console.println(r.message());
    }

    private void completeInteractive() {
        int id = console.readInt("Order ID:");
        Result r = completeOrder.execute(tech, id);
        console.println(r.message());
    }
}
