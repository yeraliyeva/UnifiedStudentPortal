package application.usecase.messaging;

import application.Result;
import domain.messaging.News;
import domain.repository.NewsRepository;
import domain.shared.IdSequence;
import domain.shared.Username;
import infrastructure.logging.Logger;

public final class PublishNews {
    private final NewsRepository news;
    private final IdSequence ids;
    private final Logger logger;

    public PublishNews(NewsRepository news, IdSequence ids, Logger logger) {
        this.news = news;
        this.ids = ids;
        this.logger = logger;
    }

    public Result execute(Username author, String title, String body, boolean pinned) {
        News n = new News(ids.next(), title, body, author, pinned);
        news.save(n);
        logger.log(author, "Published news: " + title);
        return Result.ok("News published.");
    }
}
