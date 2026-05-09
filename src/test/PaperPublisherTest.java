package test;

import application.Result;
import application.usecase.research.PublishPaper;
import application.usecase.user.BecomeResearcher;
import domain.repository.NewsRepository;
import domain.repository.NotificationRepository;
import domain.repository.ResearchPaperRepository;
import domain.repository.ResearchProjectRepository;
import domain.repository.UserRepository;
import domain.research.JournalName;
import domain.research.ResearchProject;
import domain.service.PaperPublisher;
import domain.shared.IdSequence;
import domain.user.Student;
import infrastructure.logging.RepositoryLogger;
import infrastructure.persistence.inmemory.*;

public final class PaperPublisherTest {

    public static void runAll() {
        TestRunner.run("PaperPublisher: subscriber gets notification on publish", PaperPublisherTest::testNotifies);
        TestRunner.run("PaperPublisher: publish creates news announcement", PaperPublisherTest::testNewsAnnouncement);
    }

    private static void testNotifies() {
        UserRepository users = new InMemoryUserRepository();
        ResearchPaperRepository papers = new InMemoryResearchPaperRepository();
        ResearchProjectRepository projects = new InMemoryResearchProjectRepository();
        NotificationRepository notifications = new InMemoryNotificationRepository();
        NewsRepository news = new InMemoryNewsRepository();
        var logs = new InMemoryLogRepository();

        Student subscriber = Fixtures.student("sub");
        new BecomeResearcher(new RepositoryLogger(logs)).execute(subscriber, "AI");
        users.save(subscriber);

        ResearchProject project = new ResearchProject(1, new JournalName("AI Journal"), "AI", subscriber.username());
        projects.save(project);
        subscriber.researcherProfile().subscribe("AI Journal");
        users.save(subscriber);

        Student author = Fixtures.student("auth");
        new BecomeResearcher(new RepositoryLogger(logs)).execute(author, "AI");
        users.save(author);

        PaperPublisher publisher = new PaperPublisher(papers, projects, notifications, news, users);
        PublishPaper publishPaper = new PublishPaper(publisher, new IdSequence(), new RepositoryLogger(logs));
        Result r = publishPaper.execute(author, "Hello AI", "AI Journal", "abs", 5, null);

        Assert.isTrue(r.success(), "Publish should succeed");
        Assert.isFalse(notifications.findFor(subscriber.username()).isEmpty(),
                "Subscriber should have a notification");
    }

    private static void testNewsAnnouncement() {
        UserRepository users = new InMemoryUserRepository();
        ResearchPaperRepository papers = new InMemoryResearchPaperRepository();
        ResearchProjectRepository projects = new InMemoryResearchProjectRepository();
        NotificationRepository notifications = new InMemoryNotificationRepository();
        NewsRepository news = new InMemoryNewsRepository();
        var logs = new InMemoryLogRepository();

        Student author = Fixtures.student("auth");
        new BecomeResearcher(new RepositoryLogger(logs)).execute(author, "AI");
        users.save(author);

        PaperPublisher publisher = new PaperPublisher(papers, projects, notifications, news, users);
        new PublishPaper(publisher, new IdSequence(), new RepositoryLogger(logs))
                .execute(author, "Hello AI", "AnyJournal", "abs", 5, null);

        Assert.isFalse(news.findAllSorted().isEmpty(), "Publish should create a news announcement");
    }
}
