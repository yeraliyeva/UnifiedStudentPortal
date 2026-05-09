package application.usecase.admin;

import application.Result;
import domain.messaging.News;
import domain.repository.NewsRepository;
import domain.repository.ResearchPaperRepository;
import domain.repository.UserRepository;
import domain.research.HIndex;
import domain.service.HIndexCalculator;
import domain.shared.IdSequence;
import domain.shared.Username;
import domain.user.ResearcherCapable;
import domain.user.User;
import infrastructure.logging.Logger;

import java.util.Comparator;
import java.util.Optional;

public final class GenerateTopResearcherNews {
    private final UserRepository users;
    private final ResearchPaperRepository papers;
    private final NewsRepository news;
    private final HIndexCalculator hCalc;
    private final IdSequence newsIds;
    private final Logger logger;

    public GenerateTopResearcherNews(UserRepository users, ResearchPaperRepository papers, NewsRepository news,
                                     HIndexCalculator hCalc, IdSequence newsIds, Logger logger) {
        this.users = users;
        this.papers = papers;
        this.news = news;
        this.hCalc = hCalc;
        this.newsIds = newsIds;
        this.logger = logger;
    }

    public Result execute(Username actor) {
        Optional<User> top = users.findAll().stream()
                .filter(u -> u instanceof ResearcherCapable rc && rc.isResearcher())
                .max(Comparator.comparingInt(u -> hCalc.calculate(papers.findByAuthor(u.username())).value()));
        if (top.isEmpty()) return Result.fail("No active researchers found.");
        User u = top.get();
        HIndex h = hCalc.calculate(papers.findByAuthor(u.username()));
        News n = new News(newsIds.next(),
                "Research: Top Cited Researcher — " + u.name().full(),
                u.name().full() + " (" + u.username() + ") leads with h-index " + h.value() + ".",
                new Username("system"));
        news.save(n);
        logger.log(actor, "Generated top researcher news");
        return Result.ok("Top researcher news generated.");
    }
}
