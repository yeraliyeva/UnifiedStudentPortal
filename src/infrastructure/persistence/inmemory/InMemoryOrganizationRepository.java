package infrastructure.persistence.inmemory;

import domain.organization.Organization;
import domain.repository.OrganizationRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryOrganizationRepository implements OrganizationRepository {
    private final Map<String, Organization> store = new LinkedHashMap<>();

    @Override public void save(Organization o) { store.put(o.name().toLowerCase(), o); }
    @Override public Optional<Organization> findByName(String name) {
        return Optional.ofNullable(store.get(name.toLowerCase()));
    }
    @Override public Collection<Organization> findAll() { return Collections.unmodifiableCollection(store.values()); }
}
