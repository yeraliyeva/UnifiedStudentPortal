package application.usecase.organization;

import application.Result;
import domain.organization.Organization;
import domain.repository.OrganizationRepository;
import domain.user.Student;
import infrastructure.logging.Logger;

public final class CreateOrganization {
    private final OrganizationRepository orgs;
    private final Logger logger;

    public CreateOrganization(OrganizationRepository orgs, Logger logger) {
        this.orgs = orgs;
        this.logger = logger;
    }

    public Result execute(Student head, String name) {
        if (orgs.findByName(name).isPresent()) return Result.fail("Organization already exists: " + name);
        Organization org = new Organization(name, head.username());
        orgs.save(org);
        logger.log(head.username(), "Created organization: " + name);
        return Result.ok("Organization created: " + name);
    }
}
