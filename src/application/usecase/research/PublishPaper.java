package application.usecase.research;

import application.Result;
import domain.research.JournalName;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.service.PaperPublisher;
import domain.shared.IdSequence;
import domain.shared.Username;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

public final class PublishPaper {
    private final PaperPublisher publisher;
    private final IdSequence paperIds;
    private final Logger logger;

    public PublishPaper(PaperPublisher publisher, IdSequence paperIds, Logger logger) {
        this.publisher = publisher;
        this.paperIds = paperIds;
        this.logger = logger;
    }

    public Result execute(User author, String title, String journal, String abstractText, int pages, String doi) {
        if (!(author instanceof ResearcherCapable rc) || !rc.isResearcher()) {
            return Result.fail("You must become a researcher first.");
        }
        ResearchPaper paper = new ResearchPaper(new PaperId(paperIds.next()),
                title, author.username(), new JournalName(journal),
                abstractText, pages, doi);
        publisher.publish(paper);
        logger.log(author.username(), "Published paper: " + paper.title());
        return Result.ok("Paper published: " + paper.title());
    }
}
