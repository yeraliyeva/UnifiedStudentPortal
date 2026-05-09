package infrastructure.persistence.orm.repository;

import domain.messaging.Request;
import domain.repository.RequestRepository;
import domain.shared.Username;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.RequestMapper;
import infrastructure.persistence.orm.Repository;

import java.util.List;
import java.util.Optional;

public final class OrmRequestRepository implements RequestRepository {
    private final Repository<Request, Integer> repo;

    public OrmRequestRepository(Database db) {
        this.repo = new Repository<>(db, "requests", new RequestMapper());
    }

    @Override public void save(Request r) { repo.save(r); }
    @Override public Optional<Request> findById(int id) { return repo.findById(id); }
    @Override public List<Request> findAll() { return repo.findAll(); }
    @Override public List<Request> findByRequester(Username u) {
        return repo.whereEq("requester", u.value()).list();
    }
}
