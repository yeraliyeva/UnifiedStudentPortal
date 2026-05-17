package application.usecase.messaging;

import application.Result;
import domain.messaging.Comment;
import domain.messaging.News;
import domain.repository.NewsRepository;
import domain.user.User;
import infrastructure.logging.Logger;

public final class CommentOnNews {
    private final NewsRepository news;
    private final Logger logger;

    public CommentOnNews(NewsRepository news, Logger logger) {
        this.news = news;
        this.logger = logger;
    }

    public Result execute(User user, int newsId, String text) {
        if (text == null || text.isBlank()) return Result.fail("Comment cannot be empty.");
        News n = news.findById(newsId).orElse(null);
        if (n == null) return Result.fail("News not found.");
        n.addComment(Comment.now(user.username(), text));
        news.save(n);
        logger.log(user.username(), "Commented on news #" + newsId);
        return Result.ok("Comment added.");
    }
}
