package application.usecase.research;

import application.Result;
import domain.repository.ResearchProjectRepository;
import domain.research.JournalName;
import domain.research.ResearchProject;
import domain.shared.IdSequence;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class CreateResearchProject {
    private final ResearchProjectRepository projects;
    private final IdSequence ids;
    private final Logger logger;

    public CreateResearchProject(ResearchProjectRepository projects, IdSequence ids, Logger logger) {
        this.projects = projects;
        this.ids = ids;
        this.logger = logger;
    }

    public Result execute(User actor, String journal, String topic) {
        if (!(actor instanceof ResearcherCapable rc) || !rc.isResearcher()) {
            return Result.fail("You must become a researcher first.");
        }
        ResearchProject project = new ResearchProject(ids.next(), new JournalName(journal), topic, actor.username());
        projects.save(project);
        logger.log(actor.username(), "Created research project: " + journal);
        return Result.ok("Project created: " + journal);
    }
}
