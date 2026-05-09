package domain.repository;

import domain.messaging.Request;
import domain.shared.Username;

import java.util.List;
import java.util.Optional;

public interface RequestRepository {
    void save(Request request);
    Optional<Request> findById(int id);
    List<Request> findAll();
    List<Request> findByRequester(Username requester);
}
