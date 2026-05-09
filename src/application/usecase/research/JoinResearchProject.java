package application.usecase.research;

import application.Result;
import domain.repository.ResearchProjectRepository;
import domain.research.JournalName;
import domain.research.ResearchProject;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class JoinResearchProject {
    private final ResearchProjectRepository projects;
    private final Logger logger;

    public JoinResearchProject(ResearchProjectRepository projects, Logger logger) {
        this.projects = projects;
        this.logger = logger;
    }

    public Result execute(User user, String journalName) {
        if (!(user instanceof ResearcherCapable rc) || !rc.isResearcher()) {
            return Result.fail("You must become a researcher first.");
        }
        ResearchProject project = projects.findByJournal(new JournalName(journalName)).orElse(null);
        if (project == null) return Result.fail("Project not found: " + journalName);
        project.addParticipant(user.username());
        projects.save(project);
        logger.log(user.username(), "Joined project: " + journalName);
        return Result.ok("Joined project: " + journalName);
    }
}
