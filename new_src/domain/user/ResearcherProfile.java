package domain.user;

import domain.shared.Username;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ResearcherProfile {
    private final Username owner;
    private final String field;
    private final Set<String> subscribedJournals = new LinkedHashSet<>();

    public ResearcherProfile(Username owner, String field) {
        this.owner = owner;
        this.field = field;
    }

    public Username owner() { return owner; }
    public String field() { return field; }

    public void subscribe(String journalName) { subscribedJournals.add(journalName); }
    public void unsubscribe(String journalName) { subscribedJournals.remove(journalName); }
    public Set<String> subscribedJournals() { return java.util.Collections.unmodifiableSet(subscribedJournals); }
    public boolean isSubscribedTo(String journal) { return subscribedJournals.contains(journal); }
}
