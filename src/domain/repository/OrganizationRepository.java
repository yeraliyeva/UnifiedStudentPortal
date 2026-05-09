package domain.repository;

import domain.organization.Organization;

import java.util.Collection;
import java.util.Optional;

public interface OrganizationRepository {
    void save(Organization organization);
    Optional<Organization> findByName(String name);
    Collection<Organization> findAll();
}
