package domain.service;

import domain.repository.ResearchProjectRepository;
import domain.research.JournalName;
import domain.user.ResearcherCapable;

public final class SubscriptionService {
    private final ResearchProjectRepository projects;

    public SubscriptionService(ResearchProjectRepository projects) { this.projects = projects; }

    public boolean subscribe(ResearcherCapable researcher, String journalName) {
        if (!researcher.isResearcher()) throw new IllegalStateException("user is not a researcher");
        if (projects.findByJournal(new JournalName(journalName)).isEmpty()) return false;
        researcher.researcherProfile().subscribe(journalName);
        return true;
    }

    public void unsubscribe(ResearcherCapable researcher, String journalName) {
        if (!researcher.isResearcher()) return;
        researcher.researcherProfile().unsubscribe(journalName);
    }
}
