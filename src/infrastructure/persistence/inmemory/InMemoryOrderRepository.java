package infrastructure.persistence.inmemory;

import domain.messaging.Order;
import domain.repository.OrderRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> store = new ArrayList<>();

    @Override public void save(Order o) {
        store.removeIf(x -> x.id() == o.id());
        store.add(o);
    }
    @Override public Optional<Order> findById(int id) {
        return store.stream().filter(o -> o.id() == id).findFirst();
    }
    @Override public List<Order> findAll() { return Collections.unmodifiableList(store); }
}
