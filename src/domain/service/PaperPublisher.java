package domain.service;

import domain.messaging.News;
import domain.messaging.Notification;
import domain.repository.NewsRepository;
import domain.repository.NotificationRepository;
import domain.repository.ResearchPaperRepository;
import domain.repository.ResearchProjectRepository;
import domain.repository.UserRepository;
import domain.research.ResearchPaper;
import domain.research.ResearchProject;
import domain.user.ResearcherCapable;
import domain.user.User;

public final class PaperPublisher {
    private final ResearchPaperRepository papers;
    private final ResearchProjectRepository projects;
    private final NotificationRepository notifications;
    private final NewsRepository news;
    private final UserRepository users;
    private final java.util.concurrent.atomic.AtomicInteger newsId = new java.util.concurrent.atomic.AtomicInteger(1);

    public PaperPublisher(ResearchPaperRepository papers, ResearchProjectRepository projects,
                          NotificationRepository notifications, NewsRepository news, UserRepository users) {
        this.papers = papers;
        this.projects = projects;
        this.notifications = notifications;
        this.news = news;
        this.users = users;
    }

    public void publish(ResearchPaper paper) {
        papers.save(paper);
        projects.findByJournal(paper.journal()).ifPresent(project -> {
            project.recordPublication(paper.id());
            projects.save(project);
            notifySubscribers(project, paper);
        });
        announce(paper);
    }

    private void notifySubscribers(ResearchProject project, ResearchPaper paper) {
        String text = "[JOURNAL] New paper in '" + project.journal() + "': " + paper.title();
        for (User u : users.findAll()) {
            if (u instanceof ResearcherCapable rc && rc.isResearcher()
                    && rc.researcherProfile().isSubscribedTo(project.journal().value())) {
                notifications.save(Notification.of(u.username(), text));
            }
        }
    }

    private void announce(ResearchPaper paper) {
        News announcement = new News(newsId.getAndIncrement(),
                "Research: New paper published in " + paper.journal(),
                "\"" + paper.title() + "\" by " + paper.author() + " has been published in " + paper.journal() + ".",
                paper.author(),
                true);
        news.save(announcement);
    }
}
