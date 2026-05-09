package application.usecase.organization;

import application.Result;
import domain.organization.Organization;
import domain.repository.OrganizationRepository;
import domain.user.Student;
import infrastructure.logging.Logger;

public final class JoinOrganization {
    private final OrganizationRepository orgs;
    private final Logger logger;

    public JoinOrganization(OrganizationRepository orgs, Logger logger) {
        this.orgs = orgs;
        this.logger = logger;
    }

    public Result execute(Student student, String name) {
        Organization org = orgs.findByName(name).orElse(null);
        if (org == null) return Result.fail("Organization not found.");
        if (org.isMember(student.username())) return Result.fail("Already a member.");
        org.addMember(student.username());
        orgs.save(org);
        logger.log(student.username(), "Joined organization: " + name);
        return Result.ok("Joined: " + name);
    }
}
