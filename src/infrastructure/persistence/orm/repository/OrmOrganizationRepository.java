package infrastructure.persistence.orm.repository;

import domain.organization.Organization;
import domain.repository.OrganizationRepository;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.OrganizationMapper;
import infrastructure.persistence.orm.Repository;

import java.util.Collection;
import java.util.Optional;

public final class OrmOrganizationRepository implements OrganizationRepository {
    private final Repository<Organization, String> repo;

    public OrmOrganizationRepository(Database db) {
        this.repo = new Repository<>(db, "organizations", new OrganizationMapper());
    }

    @Override public void save(Organization o) { repo.save(o); }
    @Override public Optional<Organization> findByName(String name) { return repo.findById(name.toLowerCase()); }
    @Override public Collection<Organization> findAll() { return repo.findAll(); }
}
