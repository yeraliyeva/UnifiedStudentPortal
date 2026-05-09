package infrastructure.persistence.orm.repository;

import domain.messaging.Order;
import domain.repository.OrderRepository;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.OrderMapper;
import infrastructure.persistence.orm.Repository;

import java.util.List;
import java.util.Optional;

public final class OrmOrderRepository implements OrderRepository {
    private final Repository<Order, Integer> repo;

    public OrmOrderRepository(Database db) {
        this.repo = new Repository<>(db, "orders", new OrderMapper());
    }

    @Override public void save(Order o) { repo.save(o); }
    @Override public Optional<Order> findById(int id) { return repo.findById(id); }
    @Override public List<Order> findAll() { return repo.findAll(); }
}
