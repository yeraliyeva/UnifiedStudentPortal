package infrastructure.persistence.inmemory;

import domain.messaging.Request;
import domain.repository.RequestRepository;
import domain.shared.Username;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class InMemoryRequestRepository implements RequestRepository {
    private final List<Request> store = new ArrayList<>();

    @Override public void save(Request r) {
        store.removeIf(x -> x.id() == r.id());
        store.add(r);
    }
    @Override public Optional<Request> findById(int id) {
        return store.stream().filter(r -> r.id() == id).findFirst();
    }
    @Override public List<Request> findAll() { return Collections.unmodifiableList(store); }
    @Override public List<Request> findByRequester(Username u) {
        return store.stream().filter(r -> r.requester().equals(u)).toList();
    }
}
