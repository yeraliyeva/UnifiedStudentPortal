package application.usecase.research;

import application.Result;
import domain.repository.ResearchPaperRepository;
import domain.repository.UserRepository;
import domain.research.HIndex;
import domain.service.HIndexCalculator;
import domain.shared.Username;
import domain.user.GraduateStudent;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class SetSupervisor {
    private final UserRepository users;
    private final ResearchPaperRepository papers;
    private final HIndexCalculator hIndex;
    private final Logger logger;

    public SetSupervisor(UserRepository users, ResearchPaperRepository papers,
                         HIndexCalculator hIndex, Logger logger) {
        this.users = users;
        this.papers = papers;
        this.hIndex = hIndex;
        this.logger = logger;
    }

    public Result execute(GraduateStudent grad, Username candidate) {
        User u = users.findByUsername(candidate).orElse(null);
        if (u == null) return Result.fail("Candidate not found.");
        if (!(u instanceof ResearcherCapable rc) || !rc.isResearcher())
            return Result.fail("Candidate is not an active researcher.");
        HIndex h = hIndex.calculate(papers.findByAuthor(candidate));
        if (!h.atLeast(HIndex.MIN_FOR_SUPERVISION))
            return Result.fail("Supervisor h-index must be at least " + HIndex.MIN_FOR_SUPERVISION.value()
                    + " (candidate has " + h.value() + ").");
        grad.setSupervisor(candidate);
        users.save(grad);
        logger.log(grad.username(), "Set supervisor: " + candidate);
        return Result.ok("Supervisor set: " + candidate);
    }
}
